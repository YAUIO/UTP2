import Utils.ClientHandler;
import Utils.Print;
import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;

import java.io.*;
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

    private Socket lastSocket;

    Print print;

    private Server(int port) {
        try {
            server = new ServerSocket(port);
            clients = new ArrayList<>();
            requestHandler = new RequestHandler(this);

            lastSocket = new Socket();

            print = new Print(true);

        } catch (IOException e) {
            print.formatR("Error while creating a server: " + e.getMessage());
        }
    }

    public void setActiveLog() {
        print.isActive = !print.isActive;
    }

    public void shutdown() {
        synchronized (this) {
            this.interrupt();
            try {
                this.server.close();
            } catch (IOException e) {
                print.formatR("<info> ServerSocket error while closing ");
            }
            synchronized (clients) {
                for (ClientHandler ch : clients) {
                    ch.close();
                    ch.interrupt();
                }
            }
        }
    }

    @Override
    public void run() {
        synchronized (requestHandler) {
            requestHandler.notify();
        }

        Runnable accept = getAcceptRunnable();

        print.formatR("<log> Server \"" + name + "\" is online on port " + server.getLocalPort() + "!");

        while (true) {

            Thread acceptWait = new Thread(accept);

            acceptWait.start();

            print.formatR("<log> Server is waiting for connection...");

            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException _) {
                    acceptWait.interrupt();
                    print.formatR("<log> Server interrupted");
                    break;
                }
            }

            ClientHandler handler = null;

            synchronized (lastSocket) {
                handler = new ClientHandler(lastSocket, true, print);
            }

            handler.setHandlerName(name);
            synchronized (clients) {
                if (clients.isEmpty()) {
                    handler.send("<info> Only you are currently connected to the server ");
                } else {
                    handler.send("<info> List of connected users: " + Print.toStr(clients, Thread::getName));
                }
                clients.add(handler);
                print.formatR("<info> " + clients.size() + " clients connected ");
            }
            print.formatR("<log> Server accepted a socket!");
        }

        print.formatR("<log> Server died");
    }

    private Runnable getAcceptRunnable() {
        final Server s = this;

        Runnable accept = () -> {
            try {
                synchronized (lastSocket) {
                    lastSocket = server.accept();
                    synchronized (s) {
                        s.notify();
                    }
                }

                synchronized (Thread.currentThread()) {
                    try {
                        Thread.currentThread().wait(1);
                    } catch (InterruptedException _) {
                        lastSocket.close();
                    }
                }

                print.formatR("<log> Server accept thread exited");

            } catch (IOException e) {
                print.errorR("Failed to establish connection to the client in accept Server thread");

                try {
                    lastSocket.close();
                } catch (IOException _) {

                }
            }
        };
        return accept;
    }

    public static Server createServer() {
        try {
            File cfgd = new File("src/main/java/Configurations/");

            ArrayList<String> cfgs = new ArrayList<>();

            if (cfgd.listFiles() != null) {
                Utils.Print.format("<info> Choose the server configuration");

                int c = 0;

                for (File f : cfgd.listFiles()) {
                    Utils.Print.format("(" + c + ") " + Server.cfgToString(f.getName()));
                    cfgs.add(f.getName());
                    c++;
                }
            } else {
                Utils.Print.error("No configurations found");
            }

            Scanner sc = new Scanner(System.in);

            String servername = "";

            while (!cfgs.contains(servername)) {
                Utils.Print.format("<info> Type in cfg name/number or hit enter to choose the 1st");

                Integer n = null;

                servername = sc.nextLine();

                try {
                    n = Integer.parseInt(servername);
                } catch (Exception _) {

                }

                if (servername.isEmpty()) {
                    servername = cfgs.getFirst();
                }

                if (n != null && n < cfgs.size()) {
                    servername = cfgs.get(n);
                }
            }

            return serverFromCfg(servername);
        } catch (Exception e) {
            Utils.Print.error("Failed to create a server with error: \"" + e.getMessage() + "\"");
        }

        return null;
    }

    public static Server serverFromCfg(String configuration) {
        try {
            BufferedReader in = new BufferedReader(new FileReader("src/main/java/Configurations/" + configuration));

            Server server = new Server(Integer.parseInt(in.readLine()));
            server.name = in.readLine();
            server.blacklist = new HashSet<>();
            server.blacklist.addAll(Arrays.asList(in.readLine().split(",")));

            server.print.setPrefix(Ansi.colorize(" | " + server.name + " | ", Attribute.ITALIC(), Attribute.BLACK_BACK()));

            server.start();

            return server;
        } catch (Exception e) {
            Utils.Print.error("Couldn't parse cfg: " + e.getMessage());
        }

        return null;
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

            if ((sender == null || sender.isEmpty()) && !(msg.size() > 1 && (msg.get(1).equals("send") || msg.get(1).equals("/send")))) {
                sender = first;
            }

            if (msg.size() > 1 && (msg.get(1).equals("send") || msg.get(1).equals("/send"))) {
                r += "user: \"" + first + "\" sent you: \"";

                int i = 2;

                while (i < msg.size()) {
                    r += msg.get(i) + " ";
                    i++;
                }

                r = r.substring(0, r.length() - 1);

                r += "\"";
            } else {
                r += "user: \"" + sender + "\" sent you: \"";

                sender = "";

                int i = 0;

                while (i < msg.size()) {
                    r += msg.get(i) + " ";
                    i++;
                }

                r = r.substring(0, r.length() - 1);

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
                    print.errorR("Error while waiting in RequestHandler: " + e.getMessage());
                }
            }

            HashSet<ClientHandler> toRemove = new HashSet<>();

            while (server.isAlive()) {

                synchronized (server.clients) {
                    if (!toRemove.isEmpty()) {
                        server.clients.removeAll(toRemove);
                        print.formatR("<info> " + server.clients.size() + " clients connected ");

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
                            print.formatR("<info> processing request: " + request);

                            boolean isDenied = false;

                            ArrayList<String> parsedRequest = new ArrayList<>(Arrays.stream(request.split(" ")).toList());
                            parsedRequest.removeFirst();

                            String msg = "";

                            if (state != State.Empty) {
                                if (parsedRequest.getLast().equals("CANCEL")) {
                                    for (ClientHandler client : server.clients) {
                                        if (sender.equals(client.getName())) {
                                            print.formatR("<debug> cancel operation found match of " + sender + " with " + client.getName());
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

                            if (!parsedRequest.isEmpty()) {
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
                                        print.formatR("<debug> Denied response: found match of " + parsedRequest.getFirst() + " with " + client.getName() + " ");
                                        break;
                                    }
                                }
                                continue;
                            }

                            if (parsedRequest.get(1).startsWith("/")) {
                                parsedRequest.set(1,parsedRequest.get(1).substring(1));
                            }

                            switch (parsedRequest.get(1)) {
                                case "send" -> {
                                    parsedRequest.remove(1);
                                    parsedRequest.removeFirst();
                                    for (ClientHandler client : server.clients) {
                                        if (parsedRequest.getFirst().equals(client.getName())) {
                                            print.formatR("<debug> /send found match of " + parsedRequest.getFirst() + " with " + client.getName());
                                            client.send(msg);
                                            break;
                                        }
                                    }
                                }
                                case "sendm" -> {
                                    state = State.nextSendM;
                                    sender = parsedRequest.getFirst();
                                    print.formatR("<debug> sendm sender " + sender + ", full set " + parsedRequest.toString());
                                    receivers = new HashSet<>(parsedRequest.subList(2, parsedRequest.size()));
                                    print.formatR("<debug> sendm hashset " + receivers.toString());
                                    for (ClientHandler client : server.clients) {
                                        if (sender.equals(client.getName())) {
                                            print.formatR("<debug> send found match of " + sender + " with " + client.getName());
                                            client.send("<info> Now type the message you want to send to " + Print.toStr(receivers) + ", or " + Ansi.colorize("CANCEL", Attribute.RED_BACK()) + " to abort the operation ");
                                            break;
                                        }
                                    }
                                }
                                case "sendex" -> {
                                    state = State.nextSendEx;
                                    sender = parsedRequest.getFirst();
                                    print.formatR("<debug> sendex sender " + sender + ", full set " + parsedRequest.toString());
                                    receivers = new HashSet<>(parsedRequest.subList(2, parsedRequest.size()));
                                    print.formatR("<debug> sendex hashset " + receivers.toString());
                                    for (ClientHandler client : server.clients) {
                                        if (sender.equals(client.getName())) {
                                            print.formatR("<debug> send found match of " + sender + " with " + client.getName());
                                            client.send("<info> Now type the message you want to send to everybody, excluding " + Print.toStr(receivers) + ", or " + Ansi.colorize("CANCEL", Attribute.RED_BACK()) + Ansi.colorize(" to abort the operation ", Attribute.BOLD(), Attribute.BLUE_BACK()));
                                            break;
                                        }
                                    }
                                }
                                case "banlist" -> {
                                    parsedRequest.remove(1);
                                    for (ClientHandler client : server.clients) {
                                        if (parsedRequest.getFirst().equals(client.getName())) {
                                            client.send(Print.toStr(server.blacklist));
                                            print.formatR("<debug> banlist found match of " + parsedRequest.getFirst() + " with " + client.getName());
                                            break;
                                        }
                                    }
                                }
                                default -> {
                                    if (state == State.Empty) {
                                        for (ClientHandler client : server.clients) {
                                            if (!client.getName().equals(parsedRequest.getFirst())) {
                                                client.send(msg);
                                            }
                                        }
                                    } else if (state == State.nextSendM) {
                                        state = State.Empty;
                                        for (String receiver : receivers) {
                                            for (ClientHandler client : server.clients) {
                                                if (receiver.equals(client.getName())) {
                                                    print.formatR("<debug> sendm found match of " + receiver + " with " + client.getName());
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
                                                    print.formatR("<debug> sendex found !match of " + receiver + " with " + client.getName());
                                                    client.send(msg);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (print.isActive) {
                                System.out.println();
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
            Utils.Print.error("Couldn't parse cfg: " + e.getMessage());
        }

        return 0;
    }

    public static String cfgToString(String configuration, boolean isServer) {
        try {
            BufferedReader in = new BufferedReader(new FileReader("src/main/java/Configurations/" + configuration));

            String r = "";

            if (isServer){
                r += "<serverInfo> Port: ";
                r += in.readLine();
                r += ", Name: ";
            } else {
                in.readLine();
                r += "<serverInfo> Name: ";
            }
            r += in.readLine();
            r += ", cfgName: ";
            r += configuration;

            return r;
        } catch (Exception e) {
            Utils.Print.error("Couldn't parse cfg: " + e.getMessage());
        }

        return "<error>";
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
            Utils.Print.error("Couldn't parse cfg: " + e.getMessage());
        }

        return "<error>";
    }
}
