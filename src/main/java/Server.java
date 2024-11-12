import Utils.ClientHandler;
import Utils.Print;

import javax.sound.midi.Receiver;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

public class Server extends Thread {
    String name;
    ServerSocket server;
    ArrayList<ClientHandler> clients;
    HashSet<String> blacklist;
    RequestHandler requestHandler;

    private Server(int port) {
        try {
            server = new ServerSocket(port);
            clients = new ArrayList<>();
            requestHandler = new RequestHandler(this);
        } catch (IOException e) {
            Print.error(e.getMessage() + " while creating new server");
            System.exit(0);
        }
    }

    @Override
    public void run() {
        synchronized (requestHandler) {
            requestHandler.notify();
        }

        Print.format("<log> Server \"" + name + "\" is online on port " + server.getLocalPort() + "!");

        while (true) {
            try {
                Print.format("<log> Server is waiting for connection...");
                Socket client = server.accept();
                ClientHandler handler = new ClientHandler(client, true);
                handler.setHandlerName(name);
                synchronized (clients) {
                    clients.add(handler);
                }
                Print.format("<log> Server accepted a socket!");
            } catch (IOException e) {
                Print.error("Failed to establish connection to the client in main Server thread");
            }
        }
    }

    public static void main(String[] args) {
        try {
            File cfgd = new File("src/main/java/Configurations/");

            ArrayList<String> cfgs = new ArrayList<>();

            if (cfgd.listFiles() != null) {
                Print.format("<info> Choose the server configuration");

                for (File f : cfgd.listFiles()) {
                    Print.format(Server.cfgToString(f.getName()));
                    cfgs.add(f.getName());
                }
            } else {
                Print.error("No configurations found");
                System.exit(0);
            }

            Scanner sc = new Scanner(System.in);

            String servername = "";

            while (!cfgs.contains(servername)) {
                Print.format("<info> Type in cfg name or hit enter to choose the 1st");
                servername = sc.nextLine();
                if (servername.isEmpty()) {
                    servername = cfgs.getFirst();
                }
            }

            serverFromCfg(servername);
        } catch (Exception e) {
            Print.error("Failed to create a server with error: \"" + e.getMessage() + "\"");
            System.exit(0);
        }
    }

    public static void serverFromCfg(String configuration) {
        try {
            BufferedReader in = new BufferedReader(new FileReader("src/main/java/Configurations/" + configuration));

            Server server = new Server(Integer.parseInt(in.readLine()));
            server.name = in.readLine();
            server.blacklist = new HashSet<>();
            server.blacklist.addAll(Arrays.asList(in.readLine().split(",")));

            server.start();
        } catch (Exception e) {
            Print.error("Couldn't parse cfg: " + e.getMessage());
        }
    }

    private class RequestHandler extends Thread {
        private final Server server;
        private State state;
        private String sender;
        private HashSet<String> receivers;

        enum State {
            Empty,
            nextSendM,
            nextSendEx
        }

        RequestHandler(Server server) {
            this.server = server;
            state = State.Empty;
            this.start();
        }

        private String msgToString(ArrayList<String> msg) {
            String r = "";
            String first = msg.getFirst();

            if ((sender == null || sender.isEmpty()) && !(msg.size() > 1 && msg.get(1).equals("/send"))) {
                sender = first;
                msg.removeFirst();
            }

            if (msg.size() > 1 && msg.get(1).equals("/send")) {
                r += "user: \"" + msg.getFirst() + "\" sent you: \"";

                int i = 3;

                while (i < msg.size()) {
                    r += msg.get(i);
                    i++;
                }

                r += "\"";
            } else {
                r += "user: \"" + sender + "\" sent you: \"";

                sender = "";

                int i = 0;

                while (i < msg.size()) {
                    r += msg.get(i);
                    i++;
                }

                r += "\"";
            }

            msg.addFirst(first);

            return r;
        }

        @Override
        public void run() {
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Print.error("Error while waiting in RequestHandler: " + e.getMessage());
                }
            }

            while (server.isAlive()) {
                synchronized (server.clients) {
                    for (ClientHandler handler : server.clients) {
                        String request = handler.getRequest();

                        if (request != null && !request.equals("null")) {
                            Print.format("<info> REQUEST " + request);

                            ArrayList<String> parsedRequest = new ArrayList<>(Arrays.stream(request.split(" ")).toList());
                            System.out.println(parsedRequest);
                            parsedRequest.removeFirst();

                            String msg = "";

                            if (!parsedRequest.isEmpty()){
                                msg = msgToString(parsedRequest);
                            }

                            System.out.println(parsedRequest);

                            switch (parsedRequest.get(1)) {
                                case "/send" -> {
                                    parsedRequest.remove(1);
                                    parsedRequest.removeFirst();
                                    for (ClientHandler client : server.clients) {
                                        if (parsedRequest.getFirst().equals(client.getName())) {
                                            client.send(msg);
                                            break;
                                        }
                                    }
                                }
                                case "/sendm" -> {
                                    state = State.nextSendM;
                                    sender = parsedRequest.getFirst();
                                    receivers = new HashSet<>(parsedRequest.subList(2, parsedRequest.size() - 1));
                                }
                                case "/sendex" -> {
                                    state = State.nextSendEx;
                                    sender = parsedRequest.getFirst();
                                    receivers = new HashSet<>(parsedRequest.subList(2, parsedRequest.size() - 1));
                                }
                                case "/banlist" -> {
                                    parsedRequest.remove(1);
                                    for (ClientHandler client : server.clients) {
                                        if (parsedRequest.getFirst().equals(client.getName())) {
                                            client.send(server.blacklist.toString());
                                            break;
                                        }
                                    }
                                }
                                default -> {
                                    if (state == State.Empty) {
                                        for (ClientHandler client : server.clients) {
                                            client.send(msg);
                                        }
                                    } else if (state == State.nextSendM) {
                                        state = State.Empty;
                                        for (String receiver : receivers) {
                                            for (ClientHandler client : server.clients) {
                                                if (receiver.equals(client.getName())) {
                                                    client.send(msg);
                                                    break;
                                                }
                                            }
                                        }
                                    } else if (state == State.nextSendEx) {
                                        state = State.Empty;
                                        for (String receiver : receivers) {
                                            for (ClientHandler client : server.clients) {
                                                if (!receiver.equals(client.getName())) {
                                                    client.send(msg);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static int cfgToPort(String configuration) {
        try {
            BufferedReader in = new BufferedReader(new FileReader("src/main/java/Configurations/" + configuration));

            return Integer.parseInt(in.readLine());
        } catch (Exception e) {
            Print.error("Couldn't parse cfg: " + e.getMessage());
        }

        return 0;
    }

    public static String cfgToString(String configuration) {
        try {
            BufferedReader in = new BufferedReader(new FileReader("src/main/java/Configurations/" + configuration));

            String r = "<serverInfo> Port: ";
            r += in.readLine();
            r += ", Name: ";
            r += in.readLine();
            r += ", cfgName: ";
            r += configuration;

            return r;
        } catch (Exception e) {
            Print.error("Couldn't parse cfg: " + e.getMessage());
        }

        return "<error>";
    }
}
