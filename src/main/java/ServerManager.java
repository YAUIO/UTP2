import Utils.Print;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

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

    private ArrayList<ServerRecord> servers;

    private ServerManager() {
        servers = new ArrayList<>();

        this.start();
    }

    @Override
    public void run() {
        Print.format("<info> Welcome to ServerManager Menu, written by s30174 alongside with its server and client ");
        String help = "<info> Command list: \n" +
                "<info> create <servername> - create new server \n" +
                "<info> log <number> - on/off server logs \n" +
                "<info> log <servername> - on/off server logs \n" +
                "<info> list - list existing servers \n" +
                "<info> help - list existing commands \n" +
                "<info> exit - exit from ServerManager \n" +
                "<info> note - servername is used only locally to ease your management experience";

        Print.format(help);

        Scanner kbin = new Scanner(System.in);
        String buf = "";
        boolean exit = false;

        while (true) {
            if (exit) break;

            if (kbin.hasNextLine()) {
                buf = kbin.nextLine();
            }

            if (!buf.isEmpty()) {
                ArrayList<String> parsedInput = new ArrayList<>(Arrays.stream(buf.split(" ")).toList());

                switch (parsedInput.getFirst()) {
                    case "create" -> {
                        servers.add(new ServerRecord(parsedInput.getLast()));
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
                    case "list" -> {
                        Print.format("<info> " + Print.toStr(servers, (ServerRecord r) -> {
                            return r.recordName + ", logging: " + r.server.print.isActive + ", alive: " + r.server.isAlive();
                        }, true, " \n") + " ");
                    }
                    case "help" -> {
                        Print.format(help);
                    }
                    case "exit" -> {
                        exit = true;
                    }
                    default -> {
                        Print.format("<debug> Unrecognised input ");
                    }
                }
            }
        }

        Utils.Print.format("<info> ServerManager was shut down. Have a nice day! ");

        System.exit(0);
    }
}
