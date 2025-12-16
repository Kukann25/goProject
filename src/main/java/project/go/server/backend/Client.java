package project.go.server.backend;

import java.net.Socket;
import java.io.PrintWriter;

import project.go.server.common.json.*;

public class Client implements Runnable {

    static public class Data {
        private Socket socket;
        private String clientId;

        /**
         * Initializes ClientData with the given socket.
         * @param socket The socket connected to the client.
         */
        public Data(final Socket socket) {
            this.socket = socket;
            this.clientId = java.util.UUID.randomUUID().toString();
        }

        /**
         * Gets the socket socket.
         * @return The socket connected to the client.
         */
        public Socket getSocket() {
            return socket;
        }

        /**
         * Creates a Connection JSON object for this client.
         * @return Connection object containing the clientId.
         */
        public Connection data() {
            return new Connection(this.clientId);
        }

        /**
         * Gets the unique client identifier.
         * @return The clientId string.
         */
        public String getClientId() {
            return clientId;
        }
    }

    static public enum State {
        CONNECTED,
        WAITING,
        IN_MATCH,
        DISCONNECTED
    }

    private Data clientData;
    private PrintWriter out;
    private State state;
    
    /**
     * Initializes a Client for the given socket connection.
     * @param socket The socket connected to the client.
     */
    public Client(Socket socket) {
        this.state = State.CONNECTED;
        this.clientData = new Data(socket);
    }

    @Override
    public void run() {
        // A client has connected
        try {
            out = new PrintWriter(clientData.getSocket().getOutputStream(), true);
            
            // Send the player id
            String json = JsonFmt.toJson(clientData.data());
            out.println(json);
            setState(State.WAITING);

            // wait for a match to be assigned
            while(isWaitingForMatch()) {
                // Waiting for a match
                Thread.yield();
            }

            // Joined a match, further handling will be done in Match class
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
    }

    /**
     * Closes the client connection and updates state.
     */
    final public void close() {
        try {
            clientData.getSocket().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.state = State.DISCONNECTED;
    }

    /**
     * Gets the current state of the client.
    */ 
    final public State getState() {
        return state;
    }

    /**
     * Gets the ClientData associated with this handler.
     */
    final public Data getClientData() {
        return clientData;
    }

    /**
     * Sets the state of the client handler in a thread-safe manner.
     */
    synchronized private void setState(final State newState) {
        this.state = newState;
    }

    /**
     * Marks the client as having joined a match.
     */
    final public void join() {
        setState(State.IN_MATCH);
    }

    /**
     * Checks if the client is waiting for a match (thread-safe).
     * @return true if the client is in WAITING state, false otherwise.
     */
    final public boolean isWaitingForMatch() {
        synchronized(this) {
            return this.state == State.WAITING;
        }
    }
}
