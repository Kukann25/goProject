package project.go.server.client;

/**
 * Class to manage the state of the client application.
 */
public class ClientState {
    private boolean isRunning;

    public ClientState() {
        this.isRunning = true;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void stop() {
        this.isRunning = false;
    }
}
