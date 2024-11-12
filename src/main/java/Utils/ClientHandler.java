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
        if (isServer != null && isServer) {
            Print.format("<sent> " + s.getBytes().length + " bytes ");
        } else {
            Print.format("<sent> " + s + " ");
        }
        out.println(s);
    }

    public void sendRequest(String s) {
        Print.format("<sent> " + s + " ");
        out.println("<request> " + name + " " + s);
    }

    @Override
    public void run() {
        if (isServer != null && isServer) {
            Thread thread = new Thread(new ClientRun());
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                Print.error("Thread was interrupted while joining in ClientHandler ");
            }
        } else {
            do {
                Thread thread = new Thread(new ClientRun());

                thread.start();

                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Print.error("Thread was interrupted while joining in ClientHandler ");
                }
            } while (reconnect());
        }
    }

    private boolean reconnect() {
        Print.format("<info> Do you want to attempt to reconnect to the server? y/N ");
        Scanner sc = new Scanner(System.in);

        if (sc.hasNextLine()) {
            String s = sc.nextLine();

            if (!s.isEmpty()) {
                if (s.equals("y") || s.equals("Y") || s.equals("yes")) {
                    try {
                        socket = new Socket("localhost", socket.getPort());

                        in = new InputWriterThread(this);
                        out = new PrintWriter(socket.getOutputStream(), true);

                        return true;
                    } catch (IOException e) {
                        Print.error("Failed to reconnect to \"" + socket.toString() + "\"");
                    }
                }
            }
        }

        return false;
    }

    public String getRequest() {
        synchronized (request) {
            if (!request.equals("null") && !request.isEmpty()) {
                String b = request;
                request = "null";
                return b;
            }
        }

        return null;
    }

    private class ClientRun implements Runnable {
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
                Print.format("<info> Registered previously unnamed client \"" + name + "\"");

                out.println("<info> Greetings, user " + name + " on our messaging server!\n" +
                        "<info> You can:\n" +
                        "<info> Send a message to every other connected client - default behaviour\n" +
                        "<info> /send <username> <msg> - Send a message to a specific person\n" +
                        "<info> /sendm <u1> <u2> ,then next line just <msg> - Send a message to multiple specific people\n" +
                        "<info> /sendex <u1> <u2>,then next line just <msg> - Send a message to every other connected client, with exception to some people\n" +
                        "<info> /banlist - Query the server for the list of banned phrases\n");

                out.println();
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

                Print.format("<log> Client disconnected.");
            } else { //Server mode send data
                while (in.isAlive()) {
                    String buf = null;
                    synchronized (in.lastLine) {
                        if (in.lastLine.startsWith("<request>")) {
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

                Print.format("<log> Client " + name + " disconnected.");
            }
        }
    }
}
