package project.go.server.backend;

import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import project.go.Config;

public class ClientPool {
    private final HashMap<String, Client> clients;
    private final ExecutorService clientPool;
    private boolean isRunning = true;

    public ClientPool() {
        this.clients = new HashMap<>();
        this.clientPool = java.util.concurrent.Executors.newFixedThreadPool(Config.MAX_CLIENTS);
    }

    /**
     * Adds a new client to the pool and starts its handler.
     */
    synchronized public void addClient(final Client client) {
        if (!isRunning) {
            throw new IllegalStateException("ClientPool is not running");
        }
        clients.put(client.getClientData().getClientId(), client);
        clientPool.execute(client);
    }

    /**
     * Removes a client from the pool by clientId.
     */
    synchronized public void removeClient(final String clientId) {
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

    synchronized public Vector<Client> getAwaitingClients() {
        Vector<Client> awaitingClients = new Vector<>();
        for (Client client : clients.values()) {
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
        for (Client client : clients.values()) {
            client.close();
        }

        clientPool.shutdownNow();
    }
}
