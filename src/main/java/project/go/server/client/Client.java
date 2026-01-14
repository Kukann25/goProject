package project.go.server.client;

import project.go.Config;

public class Client {

    private ClientConn connection;
    private ClientState clientState;
    private static Client clientInstance = null;

    private Client() {
        this(Config.PORT);
    }

    private Client(int port) {
        this.connection = new ClientConn(port);
        this.clientState = new ClientState();
    }

    public static Client getInstance() {
        if (clientInstance == null) {
            clientInstance = new Client();
        }
        return clientInstance;
    }
    
    public ClientConn getConnection() {
        return connection;
    }

    public void reset() {
        clientState.reset();
    }

    public ClientState getClientState() {
        return clientState;
    }
}
