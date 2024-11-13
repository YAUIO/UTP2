package Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class InputWriterThread extends Thread {
    private final BufferedReader in;
    private final ClientHandler handler;
    private Print print;
    public String lastLine;

    InputWriterThread(ClientHandler handler, Print print) throws IOException {
        in = new BufferedReader(new InputStreamReader(handler.socket.getInputStream()));
        this.handler = handler;
        lastLine = "";

        this.print = print;

        this.start();
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
                } if (!line.isEmpty()) {
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
