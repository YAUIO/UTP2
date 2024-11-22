package Utils.Terminal;

import Utils.Print;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.image.BufferedImage;

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

    public void appendImg(BufferedImage img) {
        // Convert BufferedImage to ImageIcon
        ImageIcon icon = new ImageIcon(img);

        Style style = this.addStyle("ImageStyle", null);
        StyleConstants.setIcon(style, icon);

        try {
            getStyledDocument().insertString(getStyledDocument().getLength()," ",style);
        } catch (BadLocationException _) {
        }
    }
}
