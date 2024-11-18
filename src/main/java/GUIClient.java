import Utils.Terminal.InputTerminal;
import Utils.Terminal.OutputTerminal;
import Utils.Print;

import javax.swing.*;
import java.awt.*;

public class GUIClient {
    public static void main(String[] args) {
        try {
            JFrame window = new JFrame("GUIClient");

            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setSize(800, 600);
            window.setLayout(new BorderLayout());

            OutputTerminal outTerm = new OutputTerminal();
            InputTerminal inTerm = new InputTerminal ();

            // Add them to the JFrame, one at the top and one at the bottom
            window.add(new JScrollPane(outTerm), BorderLayout.CENTER);
            window.add(inTerm, BorderLayout.SOUTH);

            // Add some sample text with ANSI codes
            outTerm.append("Welcome to Terminal 1\n", Print.info);

            // Display the JFrame
            window.setVisible(true);

            Print.out = outTerm;

            Client.client();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
