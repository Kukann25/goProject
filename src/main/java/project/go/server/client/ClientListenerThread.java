package project.go.server.client;

public class ClientListenerThread {
    private static ClientListenerThread instance = null;
    private ClientListener listener;
    private Thread listenerThread;

    private ClientListenerThread(ClientState clientState, ClientConn connData) {
        this.listener = new ClientListener(clientState, connData);
        this.listenerThread = new Thread(this.listener);
        this.listenerThread.start();
    }

    public static void init(ClientState clientState, ClientConn connData) {
        if (instance == null) {
            instance = new ClientListenerThread(clientState, connData);
        }
    }

    public static ClientListenerThread getInstance() {
        return instance;
    }

    public ClientListener getListener() {
        return instance.listener;
    }

    // Stops the listener thread and cleans up resources
    public void kill() {
        this.listener.stop();
        if (listenerThread != null) {
            try {
                listenerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            listenerThread = null;
            instance = null;
        }
    }    
}
