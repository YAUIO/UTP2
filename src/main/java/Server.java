import Utils.ClientHandler;
import Utils.Print;
import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;

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
                    if (clients.isEmpty()){
                        handler.send("<info> Only you are currently connected to the server ");
                    } else {
                        handler.send("<info> List of connected users: " + Print.toStr(clients));
                    }
                    clients.add(handler);
                    Print.format("<info> " + clients.size() + " clients connected ");
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
            msg.removeFirst();

            if ((sender == null || sender.isEmpty()) && !(msg.size() > 1 && msg.get(1).equals("/send"))) {
                sender = first;
            }

            if (msg.size() > 1 && msg.getFirst().equals("/send")) {
                r += "user: \"" + first + "\" sent you: \"";

                int i = 2;

                while (i < msg.size()) {
                    r += msg.get(i) + " ";
                    i++;
                }

                r = r.substring(0,r.length()-1);

                r += "\"";
            } else {
                r += "user: \"" + sender + "\" sent you: \"";

                sender = "";

                int i = 0;

                while (i < msg.size()) {
                    r += msg.get(i) + " ";
                    i++;
                }

                r = r.substring(0,r.length()-1);

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

            HashSet<ClientHandler> toRemove = new HashSet<>();

            while (server.isAlive()) {

                synchronized (server.clients) {
                    if (!toRemove.isEmpty()) {
                        server.clients.removeAll(toRemove);
                        Print.format("<info> " + server.clients.size() + " clients connected ");

                        for (ClientHandler client : server.clients) {
                            for (ClientHandler user : toRemove) {
                                client.send("<info> User \"" + user.getName() + "\" disconnected ");
                            }
                        }

                        toRemove.clear();
                    }

                    for (ClientHandler handler : server.clients) {
                        String request = handler.getRequest();

                        if (!handler.isAlive()) {
                            toRemove.add(handler);
                            continue;
                        }

                        if (request != null && !request.equals("null")) {
                            Print.format("<info> processing request: " + request);

                            boolean isDenied = false;

                            ArrayList<String> parsedRequest = new ArrayList<>(Arrays.stream(request.split(" ")).toList());
                            parsedRequest.removeFirst();

                            String msg = "";

                            if (state != State.Empty) {
                                if (parsedRequest.getLast().equals("CANCEL")) {
                                    for (ClientHandler client : server.clients) {
                                        if (sender.equals(client.getName())) {
                                            Print.format("<debug> cancel operation found match of " + sender + " with " + client.getName());
                                            client.send("<info> Operation cancelled");
                                            state = State.Empty;
                                            sender = "";
                                            receivers.clear();
                                            break;
                                        }
                                    }
                                    continue;
                                }
                            }

                            if (!parsedRequest.isEmpty()){
                                msg = msgToString(parsedRequest);
                            }

                            for (String banphrase : server.blacklist) {
                                if (msg.contains(banphrase)) {
                                    isDenied = true;
                                    break;
                                }
                            }

                            if (isDenied) {
                                for (ClientHandler client : server.clients) {
                                    if (parsedRequest.getFirst().equals(client.getName())) {
                                        client.send("Dear user, this phrase is banned on the server. Please read the banned phrases list: " + server.blacklist.toString());
                                        Print.format("<debug> Denied response: found match of " + parsedRequest.getFirst() + " with " + client.getName() + " ");
                                        break;
                                    }
                                }
                                continue;
                            }

                            switch (parsedRequest.get(1)) {
                                case "/send" -> {
                                    parsedRequest.remove(1);
                                    parsedRequest.removeFirst();
                                    for (ClientHandler client : server.clients) {
                                        if (parsedRequest.getFirst().equals(client.getName())) {
                                            Print.format("<debug> /send found match of " + parsedRequest.getFirst() + " with " + client.getName());
                                            client.send(msg);
                                            break;
                                        }
                                    }
                                }
                                case "/sendm" -> {
                                    state = State.nextSendM;
                                    sender = parsedRequest.getFirst();
                                    Print.format("<debug> sendm sender " + sender + ", full set " + parsedRequest.toString());
                                    receivers = new HashSet<>(parsedRequest.subList(2, parsedRequest.size()));
                                    Print.format("<debug> sendm hashset " + receivers.toString());
                                    for (ClientHandler client : server.clients) {
                                        if (sender.equals(client.getName())) {
                                            Print.format("<debug> /send found match of " + sender + " with " + client.getName());
                                            client.send("<info> Now type the message you want to send to " + Print.toStr(receivers) + ", or " + Ansi.colorize("CANCEL", Attribute.RED_BACK()) + " to abort the operation ");
                                            break;
                                        }
                                    }
                                }
                                case "/sendex" -> {
                                    state = State.nextSendEx;
                                    sender = parsedRequest.getFirst();
                                    Print.format("<debug> sendex sender " + sender + ", full set " + parsedRequest.toString());
                                    receivers = new HashSet<>(parsedRequest.subList(2, parsedRequest.size()));
                                    Print.format("<debug> sendex hashset " + receivers.toString());
                                    for (ClientHandler client : server.clients) {
                                        if (sender.equals(client.getName())) {
                                            Print.format("<debug> /send found match of " + sender + " with " + client.getName());
                                            client.send("<info> Now type the message you want to send to everybody, excluding " + Print.toStr(receivers) + ", or " + Ansi.colorize("CANCEL", Attribute.RED_BACK()) + Ansi.colorize(" to abort the operation ",Attribute.BOLD(),Attribute.BLUE_BACK()));
                                            break;
                                        }
                                    }
                                }
                                case "/banlist" -> {
                                    parsedRequest.remove(1);
                                    for (ClientHandler client : server.clients) {
                                        if (parsedRequest.getFirst().equals(client.getName())) {
                                            client.send(Print.toStr(server.blacklist));
                                            Print.format("<debug> /banlist found match of " + parsedRequest.getFirst() + " with " + client.getName());
                                            break;
                                        }
                                    }
                                }
                                default -> {
                                    if (state == State.Empty) {
                                        for (ClientHandler client : server.clients) {
                                            if (!client.getName().equals(parsedRequest.getFirst())){
                                                client.send(msg);
                                            }
                                        }
                                    } else if (state == State.nextSendM) {
                                        state = State.Empty;
                                        for (String receiver : receivers) {
                                            for (ClientHandler client : server.clients) {
                                                if (receiver.equals(client.getName())) {
                                                    Print.format("<debug> /sendm found match of " + receiver + " with " + client.getName());
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
                                                    Print.format("<debug> /sendex found !match of " + receiver + " with " + client.getName());
                                                    client.send(msg);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            System.out.println();
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
