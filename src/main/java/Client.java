import Utils.ClientHandler;
import Utils.Print;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        Socket client = null;

        try {
            File cfgd = new File("src/main/java/Configurations/");

            ArrayList<String> cfgs = new ArrayList<>();

            if (cfgd.listFiles() != null) {
                Print.format("<info> Choose the server for connection...");

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

            while (!cfgs.contains(servername)){
                Print.format("<info> Type in cfg name or hit enter to choose the 1st");
                servername = sc.nextLine();
                if (servername.isEmpty()) {
                    servername = cfgs.getFirst();
                }
            }

            client = new Socket("localhost",Server.cfgToPort(servername));

            Print.format("<info> Type in your username");

            servername = "";

            while (servername.length() < 4){
                servername = sc.nextLine();
                if (servername.length() < 4) {
                    Print.error("Minimum amount of characters: 4");
                }
            }

            new ClientHandler(client, servername);

            Print.format("<log> Client connected!");
        } catch (Exception e) {
            Print.error("Failed to create a client with error: \"" + e.getMessage() + "\"");
            System.exit(0);
        }
    }
}
