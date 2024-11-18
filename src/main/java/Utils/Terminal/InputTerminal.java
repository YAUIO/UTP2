package Utils.Terminal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputTerminal extends JTextField {
    public InputTerminal(Color c) {
        this();
        setForeground(c);
    }

    public InputTerminal() {
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
                    System.out.println(getText());
                    setText("");
                }
            }
        });
    }
}
