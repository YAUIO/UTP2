package Utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ClientHandler extends Thread {
    protected Socket socket;
    protected InputWriterThread in;
    protected PrintWriter out;
    protected String name;
    protected Boolean isServer;
    private String request;

    public ClientHandler(Socket socket, boolean isServer) {
        this(socket);
        this.isServer = isServer;
        request = "";
    }

    public ClientHandler(Socket socket, String name) {
        this(socket);
        this.setHandlerName(name);
    }

    public ClientHandler(Socket socket) {
        try {
            if (name == null) {
                name = socket.toString();
            }

            this.socket = socket;
            in = new InputWriterThread(this);
            out = new PrintWriter(socket.getOutputStream(), true);

            this.start();
        } catch (IOException e) {
            Print.error("Failed to establish connection to \"" + name + "\"");
        }
    }

    public void setHandlerName(String name) {
        this.name = name;
        this.setName(name);
    }

    public void send(String s) {
        Print.format("<sent> " + s);
        out.println(s);
    }

    public void sendRequest(String s) {
        Print.format("<sent> " + s);
        out.println("<request> " + name + " " + s);
    }

    @Override
    public void run() {
        Scanner kbin = null;

        if (isServer == null || !isServer) {
            kbin = new Scanner(System.in);
        }

        Print.format("<log> Clienthandler is active!");

        synchronized (in) {
            in.notify();
        }

        if (isServer != null && isServer) {
            out.println("Handshake received from server \"" + name + "\"");
            boolean isName = false;
            while (!isName) {
                synchronized (in.lastLine) {
                    isName = in.lastLine.startsWith("Client ");
                }
            }
            setHandlerName(Arrays.stream(in.lastLine.split(" ")).toList().get(1));
            Print.format("<info> New client \"" + name + "\"");

            out.println("<info> Greetings, user " + name + " on our messaging server!\n" +
                    "<info> You can:\n" +
                    "<info> Send a message to every other connected client - default behaviour\n" +
                    "<info> /send <username> <msg> - Send a message to a specific person\n" +
                    "<info> /sendm <u1> <u2> ,then next line just <msg> - Send a message to multiple specific people\n" +
                    "<info> /sendex <u1> <u2>,then next line just <msg> - Send a message to every other connected client, with exception to some people\n" +
                    "<info> /banlist - Query the server for the list of banned phrases\n");
        } else {
            out.println("Client " + name + " connected");
        }


        if (kbin != null) {
            while (in.isAlive()) { //Client mode send data
                if (kbin.hasNextLine()) {
                    String line = kbin.nextLine();
                    sendRequest(line);
                }
            }
        } else { //Server mode send data
            while (socket.isConnected()) {
                String buf = null;
                synchronized (in.lastLine) {
                    if (in.lastLine.startsWith("<request>")){
                        buf = in.lastLine;
                        in.lastLine = "";
                    }
                }

                if (buf != null) {
                    synchronized (request) {
                        request = buf;
                    }
                }
            }
        }

        Print.format("<log> Client " + name + " disconnected.");
    }

    public String getRequest () {
        synchronized (request) {
            if (!request.equals("null") && !request.isEmpty()){
                String b = request;
                request = "null";
                return b;
            }
        }

        return null;
    }
}
