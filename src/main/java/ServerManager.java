import Utils.Print;
import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
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
                "<info> help - list existing commands \n" +
                "<info> exit - exit from ServerManager \n" +
                Ansi.colorize("<info> note - <tag> is used only locally to ease your management experience, can also be null ", Attribute.ITALIC(), Attribute.BOLD());

        Print.format(help);

        Scanner kbin = new Scanner(System.in);
        String buf = "";
        boolean exit = false;

        while (!exit) {
            if (kbin.hasNextLine()) {
                buf = kbin.nextLine();
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
                    case "list" -> getStatus();
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

    private void getStatus() {

        StringBuilder r = new StringBuilder();

        int c = 0;

        Print.format(Ansi.colorize("(c) | " + Utils.Print.normalize("Tag", 12) + Utils.Print.normalize("Logging", 10) + Utils.Print.normalize("State", 12)
                + Utils.Print.normalize("Servername", 20) + Utils.Print.normalize("Port", 6) + Utils.Print.normalize("Active clients", 16), Attribute.BLUE_BACK()));

        Function<ServerRecord, String> proj = (ServerRecord sr) -> {
            String st = "";
            st += Utils.Print.normalize(sr.recordName, 12);
            st += Utils.Print.normalize(String.valueOf(sr.server.print.isActive), 10, true);
            st += Utils.Print.normalize(String.valueOf(sr.server.getState()), 12);
            st += Utils.Print.normalize(sr.server.name, 20);
            st += Utils.Print.normalize(String.valueOf(sr.server.server.getLocalPort()), 6);
            st += Utils.Print.normalize(String.valueOf(sr.server.clients.size()), 16);

            return st;
        };

        for (ServerRecord element : servers) {
            r.append(Ansi.colorize("(" + c + ") | ", Attribute.BLUE_BACK())).append(proj.apply(element)).append(" \n");
            c++;
        }

        if (r.length() >= 2) {
            r = new StringBuilder(r.substring(0, r.length() - 2));
        }

        Utils.Print.format(r.toString());
    }
}
