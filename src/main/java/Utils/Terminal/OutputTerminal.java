package Utils.Terminal;

import Utils.Print;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;

public class OutputTerminal extends JTextPane {

    public OutputTerminal(Color textColor){
        setEditable(false);
        setBackground(Color.BLACK);
        setForeground(textColor);
    }

    public OutputTerminal(){
        setEditable(false);
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);
    }

    public void append(String s, Color c) {
        try {
            StyleContext styleContext = new StyleContext();
            Style style = styleContext.addStyle("backgroundStyle", null);
            StyleConstants.setBackground(style, c);

            getStyledDocument().insertString(getStyledDocument().getLength(), s, style);
        } catch (Exception _){
            Print.error("Insertion failed");
        }
    }

    public void appendln(String s, Color c) {
        try {
            StyleContext styleContext = new StyleContext();
            Style style = styleContext.addStyle("backgroundStyle", null);
            StyleConstants.setBackground(style, c);

            getStyledDocument().insertString(getStyledDocument().getLength(), s + "\n", style);
        } catch (Exception _){
            Print.error("Insertion failed");
        }
    }
}
