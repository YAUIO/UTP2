import Utils.ClientHandler;
import Utils.Print;
import Utils.Terminal.InputTerminal;
import Utils.Terminal.OutputTerminal;

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    public ClientHandler clientHandler;

    public InputTerminal inTerm;
    public OutputTerminal outTerm;

    public static void main(String[] args) {
        new Client(null, null);
    }

    public Client(InputTerminal inTerm, OutputTerminal outTerm) {
        this.inTerm = inTerm;
        this.outTerm = outTerm;

        Socket client;

        try {
            File cfgd = new File("src/main/java/Configurations/");

            ArrayList<String> cfgs = new ArrayList<>();

            if (cfgd.listFiles() != null) {
                Print.format("<info> Choose the server for connection...");

                int c = 0;

                for (File f : cfgd.listFiles()) {
                    Print.format("(" + c + ") " + Server.cfgToString(f.getName(), false));
                    cfgs.add(f.getName());
                    c++;
                }
            } else {
                Print.error("No configurations found");
                System.exit(0);
            }

            Scanner sc = new Scanner(System.in);

            String servername = "";

            while (!cfgs.contains(servername)) {
                Print.format("<info> Type in cfg name/number or hit enter to choose the 1st");

                Integer n = null;

                if (Print.out == null) {
                    servername = sc.nextLine();
                } else {
                    while (inTerm == null || inTerm.staticReturn().isEmpty()) {
                        synchronized (Thread.currentThread()) {
                            Thread.currentThread().wait(100);
                        }
                    }
                    servername = inTerm.staticReturn();
                    inTerm.clearBuf();
                }

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

            client = new Socket("localhost", Server.cfgToPort(servername));

            Print.format("<info> Type in your username");

            servername = "";

            while (servername.length() < 4) {
                if (Print.out == null) {
                    servername = sc.nextLine();
                } else {
                    while (inTerm == null || inTerm.staticReturn().isEmpty()) {
                        synchronized (Thread.currentThread()) {
                            Thread.currentThread().wait(100);
                        }
                    }
                    servername = GUIClient.inTerm.staticReturn();
                    inTerm.clearBuf();
                }
                if (servername.length() < 4) {
                    Print.error("Minimal amount of characters: 4");
                }
            }

            clientHandler = new ClientHandler(client, servername, inTerm, outTerm);

            Print.format("<log> Client connected!");
        } catch (Exception e) {
            if (Print.out == null) {
                Print.error("Failed to create a client with error: \"" + e.getMessage() + "\"");
            } else {
                System.out.println("Failed to create a client with error: \"" + e.getMessage() + "\"");
            }
            System.exit(0);
        }
    }
}
