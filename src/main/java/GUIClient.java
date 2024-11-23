import Utils.Terminal.InputTerminal;
import Utils.Terminal.OutputTerminal;
import Utils.Print;

import javax.swing.*;
import java.awt.*;

public class GUIClient {
    public static InputTerminal inTerm;
    public static OutputTerminal outTerm;

    public static void main(String[] args) {
        try {
            JFrame window = new JFrame("GUIClient");

            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setSize(800, 600);
            window.setLayout(new BorderLayout());

            outTerm = new OutputTerminal();
            inTerm = new InputTerminal();

            window.add(new JScrollPane(outTerm), BorderLayout.CENTER);
            window.add(inTerm, BorderLayout.SOUTH);

            window.setVisible(true);

            Print.out = outTerm;

            Client cliClient = new Client(inTerm, outTerm);

            cliClient.clientHandler.join();

            window.dispose();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
