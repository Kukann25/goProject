package project.go.server.common.json;

// JSON representation of a connection
// Contains the clientId
// Used to send the clientId to the client upon connection
public class Connection {
    protected String clientId;

    public Connection() {
        this.clientId = "";
    }

    public Connection(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
