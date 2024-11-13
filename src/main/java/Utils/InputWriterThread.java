package Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
                if (!line.isEmpty()) {
                    receive(line);
                }
            } catch (IOException e) {
                if (e.getMessage().equals("Connection reset")) {
                    print.formatR("<info> Client " + handler.getName() + " had a connection reset ");
                    break;
                } else {
                    print.errorR(e.getMessage() + " in ClientHandler " + handler.getName() + " ");
                }
            }
        }

        print.formatR("<log> InputWriter died...");
    }
}
