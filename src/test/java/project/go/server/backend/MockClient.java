package project.go.server.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import project.go.Config;
import project.go.server.backend.Logger;

public class MockClient {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private int port;


    public MockClient() {
        this.port = Config.PORT;
    }

    public MockClient(int port) {
        this.port = port;
    }

    /**
     * Logs a message with the global Logger.
     */
    private static void log(String message) {
        Logger.getInstance().log("MockClient", message);
    }

    /**
     * Connects to the server using the port defined in Config.
     */
    public void connect() throws IOException {
        this.socket = new Socket("localhost", port);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        log("Connected to server at localhost:" + port);
    }

    /**
     * Sends a raw string message (JSON command) to the server.
     */
    public void send(String message) {
        if (out != null) {
            log("Sending: " + message);
            out.println(message);
        } else {
            System.err.println("Not connected. Cannot send message.");
        }
    }

    /**
     * Blocks until a message is received from the server, then prints and returns it.
     * @return The received message line, or null if connection is closed/error.
     */
    public String receive() {
        try {
            String line = in.readLine();
            if (line != null) {
                log("Received: " + line);
            } else {
                log("Server closed connection.");
            }
            return line;
        } catch (IOException e) {
            System.err.println("Error reading from server: " + e.getMessage());
            return null;
        }
    }

    /**
     * Closes the connection.
     */
    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                log("Connection closed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}