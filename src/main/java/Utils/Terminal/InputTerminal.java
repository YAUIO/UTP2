package Utils.Terminal;

import Utils.ClientHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputTerminal extends JTextField {
    private ClientHandler handler;
    private String lastLine;

    public InputTerminal(Color c) {
        this();
        setForeground(c);
    }

    public InputTerminal() {
        lastLine = "";
        setEditable(true);
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);
        int height = 28;
        setSize(300,height);
        setPreferredSize(new Dimension(300,height));
        setMinimumSize(new Dimension(100,height));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, height));

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    synchronized (lastLine) {
                        lastLine = getText();
                    }
                    setText("");
                }
            }
        });
    }

    public String staticReturn() {
        return lastLine;
    }

    public void clearBuf() {
        lastLine = "";
    }

    public void setHandler(ClientHandler handler){
        this.handler = handler;
    }
}
