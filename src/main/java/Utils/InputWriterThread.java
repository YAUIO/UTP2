package Utils;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

public class InputWriterThread extends Thread {
    private final BufferedReader in;
    private final ClientHandler handler;
    private Print print;
    private ArrayList<String> stack;
    public String lastLine;
    private BufferedImage lastImg;

    InputWriterThread(ClientHandler handler, Print print) throws IOException {
        in = new BufferedReader(new InputStreamReader(handler.socket.getInputStream()));
        this.handler = handler;
        lastLine = "";
        stack = new ArrayList<>();

        this.print = print;

        this.start();
    }

    public void download() {
        if (lastImg != null) {
            try {
                File out = new File("Downloads/" + LocalDateTime.now().toString().replace(':','-') + ".png");
                if (!out.createNewFile()) {
                    print.errorR("Error while creating a file");
                    return;
                }
                ImageIO.write(lastImg, "PNG", out);
                print.formatR("<info> Downloaded to: " + out.getAbsolutePath());
            } catch (IOException e) {
                print.errorR("Error while writing a file: " + e.getMessage());
            }
        }
    }

    public void setPrint(Print print) {
        this.print = print;
    }

    private void receive(String s) {
        synchronized (lastLine) {
            lastLine = s;
        }
        if (s.contains("<info>")) {
            print.formatR(s + " ");
        } else if (handler.isServer != null && handler.isServer) {
            print.formatR("<received> " + s.getBytes().length + " bytes ");
        } else if (s.contains("<img>")) {
            ArrayList<String> split = new ArrayList<>(Arrays.stream(s.split("<request> ")).toList());
            String fline = split.getFirst();
            print.formatR("<received> " + s.substring(0, fline.indexOf("you:") + 4));
            split.removeFirst();
            int source = 0;
            for (String line : split) {
                if (line.contains("origImg")) {
                    break;
                }
                source++;
                System.out.println(line);
            }

            print.formatR("<info> To download the image in better quality, type " + Ansi.colorize("DOWNLOAD", Attribute.RED_BACK()));

            lastImg = new BufferedImage(split.get(source).split(" ").length-1, split.size() - source, BufferedImage.TYPE_INT_ARGB);

            int x;
            int y = 0;

            while (source < split.size()) {
                x = 0;
                for (String pixel : split.get(source).split(" ")) {
                    int p = 0;
                    try {
                        p = Integer.parseInt(pixel);
                    } catch (Exception _) {
                        continue;
                    }
                    lastImg.setRGB(x, y, p);
                    x++;
                }
                y++;
                source++;
            }

        } else {
            print.formatR("<received> " + s + " ");
        }
    }

    @Override
    public void run() {
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            print.errorR("InputWriter error: " + e.getMessage());
        }

        print.formatR("<log> InputWriter is active!");

        while (handler.isAlive()) {
            try {
                String line = in.readLine();
                if (line == null) {
                    Print.format("<info> Server is not active, terminating...");
                    break;
                }
                if (!line.isEmpty()) {
                    receive(line);
                }

                try {
                    synchronized (this) {
                        this.wait(1);
                    }
                } catch (InterruptedException e) {
                    print.formatR("<info> InputWriter interrupted");
                }

            } catch (IOException e) {
                if (e.getMessage().equals("Connection reset")) {
                    print.formatR("<info> Client " + handler.getName() + " had a connection reset ");
                    break;
                } else if (e.getMessage().equals("Socket closed")) {
                    break;
                } else {
                    print.errorR(e.getMessage() + " in ClientHandler " + handler.getName() + " ");
                }
            }
        }

        print.formatR("<log> InputWriter died...");
    }
}
