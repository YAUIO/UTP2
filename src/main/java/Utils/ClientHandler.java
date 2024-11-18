package Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ClientHandler extends Thread {
    public Socket socket;
    protected InputWriterThread in;
    protected PrintWriter out;
    protected String name;
    protected Boolean isServer;
    private String request;
    private Print print;
    protected BufferedReader kbin;

    public ClientHandler(Socket socket, boolean isServer, Print print) {
        this(socket);
        this.isServer = isServer;
        request = "";
        this.print = print;
        in.setPrint(print);
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

            this.print = new Print(true);

            in = new InputWriterThread(this, print);
            out = new PrintWriter(socket.getOutputStream(), true);

            this.start();
        } catch (IOException e) {
            print.errorR("Failed to establish connection to \"" + name + "\"");
        }
    }

    public void setHandlerName(String name) {
        this.name = name;
        this.setName(name);
    }

    public void send(String s) {
       if (isServer != null && isServer) {
            print.formatR("<sent> " + s.getBytes().length + " bytes ");
        } else {
            print.formatR("<sent> " + s + " ");
        }
        out.println(s);
    }

    public void sendRequest(String s) {
        if (s.contains("<img>")) {
            if (s.contains(".jpg") || s.contains(".png") || s.contains(".jpeg")) {
                ArrayList<String> split = new ArrayList<>(Arrays.stream(s.split(" ")).toList());

                String command = split.getFirst();
                StringBuilder path = new StringBuilder();
                StringBuilder receivers = new StringBuilder();

                int imgStart = split.indexOf("<img>");

                for (int i = 1; i < imgStart; i++) {
                    receivers.append(split.get(i)).append(" ");
                }

                for (int i = imgStart+1; i<split.size(); i++) {
                    path.append(split.get(i));
                }

                CLImage img;
                try {
                    img = new CLImage(path.toString());
                    print.formatR("<debug> Parsed image at path: " + s);
                } catch (IOException e) {
                    print.errorR("Error while parsing the image: " + e.getMessage());
                    return;
                }
                s = img.toString(80,80) + img.toString(true);
                print.formatR("<info> Sent the image to the server ");
                out.println("<request> " + name + " " + command + " " + receivers + " <img>" + s);
                return;
            } else {
                print.errorR("Unsupported image type");
                return;
            }
        } else if (s.equals("DOWNLOAD")) {
            in.download();
            return;
        }

        print.formatR("<sent> " + s + " ");
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
                print.errorR("Thread was interrupted while joining in ClientHandler (no attempt to reconnect) ");
            }
        } else {
            do {
                Thread thread = new Thread(new ClientRun());

                thread.start();

                try {
                    thread.join();
                } catch (InterruptedException e) {
                    print.errorR("Thread was interrupted while joining in ClientHandler, reconnecting... ");
                }
            } while (reconnect());
        }

        try {
            socket.close();
        } catch (IOException e) {
            print.errorR(" Couldn't close a socket in " + name);
        }

        print.formatR("<log> Client " + name + " died");
        in.interrupt();
    }

    private boolean reconnect() {
        print.formatR("<info> Do you want to attempt to reconnect to the server? y/N ");
        Scanner sc = new Scanner(System.in);

        if (sc.hasNextLine()) {
            String s = sc.nextLine();

            if (!s.isEmpty()) {
                if (s.equals("y") || s.equals("Y") || s.equals("yes")) {
                    try {
                        socket = new Socket("localhost", socket.getPort());

                        in = new InputWriterThread(this, print);
                        out = new PrintWriter(socket.getOutputStream(), true);

                        return true;
                    } catch (IOException e) {
                        print.errorR("Failed to reconnect to \"" + socket.toString() + "\"");
                    }
                }
            }
        }

        return false;
    }

    public void close() {
        try {
            socket.close();
            in.interrupt();
        } catch (IOException e) {
            print.errorR("Error while closing socket: " + e.getMessage() + ", " + name);
        }
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
            kbin = null;

            if (isServer == null || !isServer) {
                kbin = new BufferedReader(new InputStreamReader(System.in));
            }

            print.formatR("<log> Clienthandler is active!");

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
                print.formatR("<info> Registered previously unnamed client \"" + name + "\"");

                out.println("<info> Greetings, user " + name + " on our messaging server!\n" + //TODO implement text mixed with images
                        "<info> You can:\n" +
                        "<info> Send a message to every other connected client - default behaviour\n" +
                        "<info> send <username> <msg> - Send a message to a specific person\n" +
                        "<info> sendm <u1> <u2> ,then next line just <msg> - Send a message to multiple specific people\n" +
                        "<info> sendex <u1> <u2>,then next line just <msg> - Send a message to every other connected client, with exception to some people\n" +
                        "<info> banlist - Query the server for the list of banned phrases\n" +
                        "<info> <msg> can be replaced either with <img> path/to/file, or plain text \n");

                out.println();
            } else {
                out.println("Client " + name + " connected");
            }


            if (kbin != null) {
                while (in.isAlive()) { //Client mode send data

                    try {
                        if (kbin.ready()) {
                            String line = kbin.readLine();
                            if (line.equals("exit")) {
                                break;
                            } else {
                                sendRequest(line);
                            }
                        }
                    } catch (IOException e) {
                        print.errorR("kbin error: " + e.getMessage());
                    }
                }

                print.formatR("<log> Client disconnected.");
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

                print.formatR("<log> Client " + name + " disconnected.");
            }
        }
    }
}
