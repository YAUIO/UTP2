import Utils.ClientHandler;
import Utils.Print;
import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

public class ServerManager extends Thread {
    public static void main(String[] args) {
        new ServerManager();
    }

    private class ServerRecord {
        public String recordName;
        public Server server;

        ServerRecord(String recordName) {
            this.server = Server.createServer();
            this.recordName = recordName;
        }
    }

    private final ArrayList<ServerRecord> servers;

    private ServerManager() {
        servers = new ArrayList<>();

        this.start();
    }

    @Override
    public void run() {
        Print.format("<info> Welcome to ServerManager Menu, written by s30174 alongside with its server and client ");
        String help = "<info> Command list: \n" +
                "<info> create <tag> - create new server \n" +
                "<info> remove <tag>/<number> - shutdown and remove a server \n" +
                "<info> log <tag>/<number> - on/off server logs \n" +
                "<info> list - list existing servers \n" +
                "<info> list <tag>/<number> clients - list connected clients \n" +
                "<info> help - list existing commands \n" +
                "<info> exit - exit from ServerManager \n" +
                Ansi.colorize("<info> note - <tag> is used only locally to ease your management experience, can also be null ", Attribute.ITALIC(), Attribute.BOLD());

        Print.format(help);

        BufferedReader kbin = new BufferedReader(new InputStreamReader(System.in));
        String buf = "";
        boolean exit = false;

        while (!exit) {
            try {
                if (kbin.ready()) {
                    buf = kbin.readLine();
                }
            } catch (IOException e) {
                Print.error("Error while reading a line in SM: " + e.getMessage());
            }

            if (!buf.isEmpty()) {
                ArrayList<String> parsedInput = new ArrayList<>(Arrays.stream(buf.split(" ")).toList());

                switch (parsedInput.getFirst()) {
                    case "create" -> {
                        servers.add(new ServerRecord(parsedInput.getLast()));
                        if (servers.size() == 1) {
                            setAllPrefixBool(false);
                        } else if (servers.size() == 2) {
                            setAllPrefixBool(true);
                        }
                    }
                    case "log" -> {
                        Integer n = null;

                        try {
                            n = Integer.parseInt(parsedInput.getLast());
                        } catch (Exception _) {

                        }

                        if (n != null && n < servers.size()) {
                            servers.get(Integer.parseInt(parsedInput.getLast())).server.setActiveLog();
                            Print.format("<info> Logging for server " + servers.get(Integer.parseInt(parsedInput.getLast())).recordName + " is now set to " + servers.get(Integer.parseInt(parsedInput.getLast())).server.print.isActive + " ");
                        } else {
                            int i = 0;
                            for (ServerRecord sr : servers) {
                                if (sr.recordName.equals(parsedInput.getLast())) {
                                    servers.get(i).server.setActiveLog();
                                    Print.format("<info> Logging for server " + servers.get(i).recordName + " is now set to " + servers.get(i).server.print.isActive + " ");
                                    break;
                                }
                                i++;
                            }
                        }
                    }
                    case "list" -> getStatus(parsedInput);
                    case "help" -> Print.format(help);
                    case "remove" -> {
                        Integer n = null;

                        try {
                            n = Integer.parseInt(parsedInput.getLast());
                        } catch (Exception _) {

                        }

                        if (n != null && n < servers.size()) {
                            servers.get(Integer.parseInt(parsedInput.getLast())).server.shutdown();
                            servers.remove(Integer.parseInt(parsedInput.getLast()));
                        } else {
                            int i = 0;
                            for (ServerRecord sr : servers) {
                                if (sr.recordName.equals(parsedInput.getLast())) {
                                    servers.get(i).server.shutdown();
                                    servers.remove(i);
                                    break;
                                }
                                i++;
                            }
                        }

                        if (servers.size() == 1) {
                            setAllPrefixBool(false);
                        }
                    }
                    case "exit" -> exit = true;
                    default -> Print.format("<debug> Unrecognised input ");
                }

                buf = "";
            }
        }

        Utils.Print.format("<info> ServerManager was shut down. Have a nice day! ");

        System.exit(0);
    }

    private void setAllPrefixBool(boolean b) {
        for (ServerRecord sr : servers) {
            sr.server.print.setPrefixAddition(b);
        }
    }

    private void getStatus(ArrayList<String> parsedInput) {

        if (parsedInput.size() == 1) {

            StringBuilder r = new StringBuilder();

            int c = 0;

            Print.format(Ansi.colorize("(c) | " + Utils.Print.normalize("Tag", 12) + Utils.Print.normalize("Logging", 10) + Utils.Print.normalize("State", 12)
                    + Utils.Print.normalize("Servername", 20) + Utils.Print.normalize("Port", 6) + Utils.Print.normalize("Active clients", 16), Print.getBackAttribute(Print.info)));

            Function<ServerRecord, String> proj = (ServerRecord sr) -> {

                return Print.normalize(sr.recordName, 12) +
                        Print.normalize(String.valueOf(sr.server.print.isActive), 10, true) +
                        Print.normalize(String.valueOf(sr.server.getState()), 12) +
                        Print.normalize(sr.server.name, 20) +
                        Print.normalize(String.valueOf(sr.server.server.getLocalPort()), 6) +
                        Print.normalize(String.valueOf(sr.server.clients.size()), 16);
            };

            for (ServerRecord element : servers) {
                r.append(Ansi.colorize("(" + c + ") | ", Print.getBackAttribute(Print.info))).append(proj.apply(element)).append(" \n");
                c++;
            }

            if (r.length() >= 2) {
                r = new StringBuilder(r.substring(0, r.length() - 2));
            }

            Utils.Print.format(r.toString());

        } else if (parsedInput.size() > 2) {
            if (parsedInput.getLast().equals("clients")) {
                Integer n = null;

                try {
                    n = Integer.parseInt(parsedInput.get(1));
                } catch (Exception _) {

                }

                synchronized (servers) {
                    if (n != null && n < servers.size()) {
                        Print.format(Ansi.colorize("(c) | " + Utils.Print.normalize("Username", 12) + Utils.Print.normalize("State", 12)
                                + Utils.Print.normalize("Port", 6), Print.getBackAttribute(Print.info)));
                        int c = 0;
                        for (ClientHandler ch : servers.get(n).server.clients) {
                            StringBuilder line = new StringBuilder();
                            line.append(Ansi.colorize("(" + c + ") | ", Print.getBackAttribute(Print.info)));
                            line.append(Print.normalize(ch.getName(), 12));
                            line.append(Utils.Print.normalize(String.valueOf(ch.getState()), 12));
                            line.append(Utils.Print.normalize(String.valueOf(ch.socket.getPort()), 6));
                            Ansi.colorize(String.valueOf(line), Print.getBackAttribute(Print.info));
                            Print.format(line.toString());
                            c++;
                        }
                    }
                }
            }
        }
    }
}
