package Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputWriterThread extends Thread {
    private final BufferedReader in;
    private final ClientHandler handler;
    public String lastLine;

    InputWriterThread(ClientHandler handler) throws IOException {
        in = new BufferedReader(new InputStreamReader(handler.socket.getInputStream()));
        this.handler = handler;
        lastLine = "";

        this.start();
    }

    private void receive (String s) {
        synchronized (lastLine) {
            lastLine = s;
        }
        if (s.contains("<info>")){
            Print.format(s + " ");
        } else if (handler.isServer != null && handler.isServer){
            Print.format("<received> " + s.getBytes().length + " bytes ");
        } else {
            Print.format("<received> " + s + " ");
        }
    }

    @Override
    public void run() {
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            Print.error("InputWriter error: " + e.getMessage());
        }

        Print.format("<log> InputWriter is active!");

        while (handler.isAlive()) {
            try {
                String line = in.readLine();
                if (!line.isEmpty()) {
                    receive(line);
                }
            } catch (IOException e) {
                if (e.getMessage().equals("Connection reset")) {
                    Print.format("<info> Client " + handler.getName() + " had a connection reset ");
                    break;
                } else {
                    Print.error(e.getMessage() + " in ClientHandler " + handler.getName() + " ");
                }
            }
        }

        Print.format("<log> InputWriter died...");
    }
}
