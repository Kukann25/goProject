package project.go.server.backend;

import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import project.go.Config;

public class ClientPool {

    static private class ClientRunWrapper implements Runnable {
        private final ConnectedClient client;
        private final ClientPool clientPool;

        public ClientRunWrapper(ConnectedClient client, ClientPool clientPool) {
            this.client = client;
            this.clientPool = clientPool;
        }

        @Override
        public void run() {
            client.run();

            // After client closes, remove it from the pool
            if (!client.getClientData().getSocket().isConnected() && !clientPool.isShutdown()) {
                Logger.getInstance().log("ClientPool", "Removing client " + client.getClientData().getClientId());
                clientPool.removeClient(client.getClientData().getClientId());
            }
        }
    }

    private final HashMap<String, ConnectedClient> clients;
    private final ExecutorService clientPool;
    private boolean isRunning = true;

    public ClientPool() {
        this.clients = new HashMap<>();
        this.clientPool = java.util.concurrent.Executors.newFixedThreadPool(Config.MAX_CLIENTS);
    }

    /**
     * Adds a new client to the pool and starts its handler.
     */
    synchronized public void addClient(final ConnectedClient client) throws IllegalStateException {
        if (!isRunning) {
            throw new IllegalStateException("ClientPool is not running");
        }
        clients.put(client.getClientData().getClientId(), client);
        clientPool.execute(new ClientRunWrapper(client, this));
    }

    /**
     * Removes a client from the pool by clientId.
     */
    synchronized public void removeClient(final String clientId) throws IllegalStateException {
        if (!isRunning) {
            throw new IllegalStateException("ClientPool is not running");
        }
        clients.remove(clientId);
    }

    /**
     * Checks if the client pool is running.
     */
    synchronized public boolean isShutdown() {
        return !isRunning;
    }

    synchronized public Vector<ConnectedClient> getAwaitingClients() {
        Vector<ConnectedClient> awaitingClients = new Vector<>();
        for (ConnectedClient client : clients.values()) {
            if (client.isWaitingForMatch()) {
                awaitingClients.add(client);
            }
        }
        return awaitingClients;
    }

    /**
     * Shuts down the client pool and stops all client handlers.
     */
    synchronized public void shutdown() {
        if (!isRunning) {
            return;
        }

        isRunning = false;
        for (ConnectedClient client : clients.values()) {
            client.close();
        }

        clientPool.shutdownNow();
    }
}
