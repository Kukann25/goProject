package project.go.server.client;

import java.util.Scanner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import project.go.applogic.MoveConverter;
import project.go.applogic.MoveHandler;
import project.go.server.common.json.Connection;
import project.go.server.common.json.GameResponse;
import project.go.server.common.json.JsonFmt;
import project.go.server.common.json.GameResponse.BoardUpdate;

/**
 * Class to listen for server messages and handle them accordingly.
 */
public class ClientListener implements Runnable {
    private ClientState clientState;
    private ClientConn connData;
    private Scanner in;

    public ClientListener(ClientState clientState, ClientConn connData) {
        this.clientState = clientState;
        this.connData = connData;
        this.in = null;
    }

    @Override
    public void run() {
        while (clientState.isRunning()) {
            if (connData.getSocket() == null || connData.getSocket().isClosed()) {
                Thread.yield();
                continue;
            }

            if (in == null) {
                try {
                    in = new Scanner(connData.getSocket().getInputStream());
                } catch (Exception e) {
                    SyncPrinter.error("Failed to get input stream from server: " + e.getMessage());
                    connData.close();
                    in = null;
                    return;
                }
            }

            try {
                if (connData.getSocket().getInputStream().available() == 0) {
                    Thread.yield();
                    continue;
                }

                String line = in.nextLine();
                parseResponse(line);
            } catch (Exception e) {
                SyncPrinter.error("Error reading from server: " + e.getMessage());
                clientState.stop();
            }
        }
    }

    private void parseResponse(String line) throws JsonProcessingException {
        try {
            // Parse into a generic tree first to inspect fields
            JsonNode node = JsonFmt.readTree(line);

            // Check for unique fields to determine the type
            if (node.has("clientId") && !node.has("type")) {
                // It's a Connection object
                Connection conn = JsonFmt.treeToValue(node, Connection.class);
                handleConnection(conn);
            } else if (node.has("type") || node.has("status")) {
                // It's a GameResponse object
                GameResponse<?> resp = JsonFmt.treeToValue(node, GameResponse.class);
                handleGameResponse(resp);
            } else {
                SyncPrinter.detail("Unknown JSON message format: " + line);
            }
        } catch (JsonParseException e) {
            SyncPrinter.error("Received malformed JSON from server: " + line);
        }
    }

    private void handleConnection(Connection conn) {
        SyncPrinter.info("Connected to the server: " + conn.getClientId());
        connData.setConnection(conn);
    }

    private void handleGameResponse(GameResponse<?> resp) {
        if (resp.isError()) {
            SyncPrinter.error("Server error: " + resp.getMessage());
            return;
        }

        MoveHandler handler = new MoveHandler(clientState.getBoard());
        switch (resp.getType()) {
            // Simple status message
            case GameResponse.TYPE_STATUS:
                SyncPrinter.success(resp.getMessage());
                break;

            // Received confirmation of valid move, has data about the move
            case GameResponse.TYPE_VALID_MOVE:
                if (!(resp.getData() instanceof GameResponse.BoardUpdate)) {
                    SyncPrinter.error("Invalid move data from server.");
                    return;
                }
                
                // Update the board and print it
                handler.makeMove(MoveConverter.fromJSON(
                    ((BoardUpdate)resp.getData()).getMove()), clientState.getPlayerColor());
                SyncPrinter.success(resp.getMessage());
                BoardPrinter.printBoard(clientState.getBoard());
                break;

            // Joined match confirmation with assigned side
            case GameResponse.TYPE_PLAYER_TURN:
                if (!(resp.getData() instanceof GameResponse.PlayerTurn)) {
                    SyncPrinter.error("Invalid player turn data from server.");
                    return;
                }
                GameResponse.PlayerTurn playerTurn = (GameResponse.PlayerTurn) resp.getData();
                clientState.setPlayerColor(playerTurn.getColor());
                SyncPrinter.success("Joined match, your side: " + playerTurn.getSide());
                BoardPrinter.printBoard(clientState.getBoard());
                break;
            
            // Opponent's move update
            case GameResponse.TYPE_BOARD_UPDATE:
                if (!(resp.getData() instanceof GameResponse.BoardUpdate)) {
                    SyncPrinter.error("Invalid board update data from server.");
                    return;
                }
                GameResponse.BoardUpdate boardUpdate = (GameResponse.BoardUpdate) resp.getData();

                // Update client state/board as needed
                handler.makeMove(MoveConverter.fromJSON(boardUpdate.getMove()), clientState.getEnemyColor());

                // Print updated board
                SyncPrinter.detail("Opponen moved: " + boardUpdate.getMove());
                BoardPrinter.printBoard(clientState.getBoard());                
                break;

            default:
                SyncPrinter.detail("Unknown response type: " + resp.getType());
                break;
        }
    }
}
