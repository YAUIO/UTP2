package Utils.Terminal;

import Utils.Print;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class OutputTerminal extends JTextPane {

    public OutputTerminal() {
        setEditable(false);
        setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);
    }

    public void append(String s, Color c) {
        try {
            StyleContext styleContext = new StyleContext();
            Style style = styleContext.addStyle("backgroundStyle", null);
            StyleConstants.setBackground(style, c);

            getStyledDocument().insertString(getStyledDocument().getLength(), s, style);
        } catch (Exception _) {
            Print.error("Insertion failed");
        }
    }

    private void appendln() {
        try {
            StyleContext styleContext = new StyleContext();
            Style style = styleContext.addStyle("null", null);
            getStyledDocument().insertString(getStyledDocument().getLength(), "\n", style);

        } catch (Exception _) {
            Print.error("Insertion failed");
        }
    }

    public void appendln(String s, Color c) {
        try {

            if (s.contains("[41m")) {
                append(s.substring(0,s.indexOf("[41m")),c);
                append(s.substring(s.indexOf("[41m")+4,s.indexOf("[0m")),Color.RED);
                s = s.substring(s.indexOf("[0m")+3);
            }

            StyleContext styleContext = new StyleContext();
            Style style = styleContext.addStyle("backgroundStyle", null);
            StyleConstants.setBackground(style, c);

            getStyledDocument().insertString(getStyledDocument().getLength(), s + "\n", style);

            SwingUtilities.invokeLater(this::full);
        } catch (Exception _) {
            Print.error("Insertion failed");
        }
    }

    public void appendImg(BufferedImage img) {
        // Convert BufferedImage to ImageIcon
        ImageIcon icon = new ImageIcon(img);

        Style style = this.addStyle("ImageStyle", null);
        StyleConstants.setIcon(style, icon);

        try {
            getStyledDocument().insertString(getStyledDocument().getLength(), " ", style);
            appendln();
            SwingUtilities.invokeLater(() -> {
                revalidate();
                repaint();
                this.full();
            });
        } catch (BadLocationException _) {
        }
    }

    private void full() {
        Container parent = getParent();
        if (parent != null && parent.getParent() instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) parent.getParent();
            SwingUtilities.invokeLater(() -> {
                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                scrollPane.revalidate();
                scrollPane.repaint();
            });
        }
        revalidate();
        repaint();
    }
}
