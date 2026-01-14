package project.go.server.client;

import java.util.Scanner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import project.go.server.client.handlers.ResponseDispatcher;
import project.go.server.common.json.Connection;
import project.go.server.common.json.GameResponse;
import project.go.server.common.json.JsonFmt;

/**
 * Class to listen for server messages and handle them accordingly.
 */
public class ClientListener implements Runnable {
    private final ClientState clientState;
    private final ClientConn connData;
    private Scanner in;
    private ResponseDispatcher dispatcher;

    public ClientListener(ClientState clientState, ClientConn connData) {
        this.clientState = clientState;
        this.connData = connData;
        this.in = null;
        this.dispatcher = new ResponseDispatcher();
    }

    public void setDispatcher(ResponseDispatcher dispatcher) {
        synchronized (this) {
            this.dispatcher = dispatcher;
        }
    }

    public void stop() {
        clientState.stop();
        connData.close();
        in = null;      
    }

    public void reset() {
        clientState.reset();
        this.dispatcher = new ResponseDispatcher();
        this.in = null;
        this.connData.close();
    }

    @Override
    public void run() {
        while (clientState.isRunning()) {
            if (!ensureConnection()) {
                Thread.yield();
                continue;
            }

            try {
                if (connData.getSocket().getInputStream().available() > 0) {
                    String line = in.nextLine();
                    parseResponse(line);
                } else {
                    Thread.yield();
                }
            } catch (Exception e) {
                SyncPrinter.error("[ClientListener] Error reading from server: " + e.getMessage());
                clientState.stop();
            }
        }
    }

    private boolean ensureConnection() {
        if (connData.getSocket() == null || connData.getSocket().isClosed()) {
            return false;
        }
        if (in == null) {
            try {
                in = new Scanner(connData.getSocket().getInputStream());
            } catch (Exception e) {
                SyncPrinter.error("Failed to get input stream: " + e.getMessage());
                connData.close();
                in = null;
                return false;
            }
        }
        return true;
    }

    private void parseResponse(String line) throws JsonProcessingException {
        synchronized (this) {
            try {
                JsonNode node = JsonFmt.readTree(line);

                // Distinguish between initial Connection handshake and Game Responses
                if (node.has("clientId") && !node.has("type")) {
                    Connection conn = JsonFmt.treeToValue(node, Connection.class);
                    handleConnection(conn);
                } else if (node.has("type") || node.has("status")) {
                    GameResponse<?> resp = JsonFmt.treeToValue(node, GameResponse.class);
                    dispatcher.dispatch(resp, clientState);
                } else {
                    SyncPrinter.detail("Unknown JSON message format: " + line);
                }
            } catch (JsonParseException e) {
                SyncPrinter.error("Received malformed JSON from server: " + line);
            }
        }
    }
    

    private void handleConnection(Connection conn) {
        SyncPrinter.info("Connected to the server: " + conn.getClientId());
        connData.setConnection(conn);
    }
}
