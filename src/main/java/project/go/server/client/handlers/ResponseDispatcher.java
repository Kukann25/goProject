package project.go.server.client.handlers;

import java.util.HashMap;
import java.util.Map;

import project.go.applogic.MoveConverter;
import project.go.applogic.MoveHandler;
import project.go.server.client.BoardPrinter;
import project.go.server.client.ClientState;
import project.go.server.client.SyncPrinter;
import project.go.server.common.json.GameResponse;
import project.go.server.common.json.GameResponse.BoardUpdate;
import project.go.server.common.json.GameResponse.PlayerTurn;

public class ResponseDispatcher {
    private final Map<String, ResponseHandler> handlers = new HashMap<>();

    public ResponseDispatcher() {
        registerHandlers();
    }

    private void registerHandlers() {
        // Status Handler
        handlers.put(GameResponse.TYPE_STATUS, (resp, state) -> {
            SyncPrinter.success(resp.getMessage());
        });

        // Valid Move Handler
        handlers.put(GameResponse.TYPE_VALID_MOVE, (resp, state) -> {
            if (resp.getData() instanceof BoardUpdate) {
                BoardUpdate data = (BoardUpdate) resp.getData();
                MoveHandler handler = new MoveHandler(state.getBoard());
                
                handler.makeMove(MoveConverter.fromJSON(data.getMove()), state.getPlayerColor());
                SyncPrinter.success(resp.getMessage());
                BoardPrinter.printBoard(state.getBoard());
            } else {
                SyncPrinter.error("Invalid move data structure.");
            }
        });

        // Player Turn Handler
        handlers.put(GameResponse.TYPE_PLAYER_TURN, (resp, state) -> {
            if (resp.getData() instanceof PlayerTurn) {
                PlayerTurn data = (PlayerTurn) resp.getData();
                state.setPlayerColor(data.getColor());
                SyncPrinter.success("Joined match, your side: " + data.getSide());
                BoardPrinter.printBoard(state.getBoard());
            } else {
                SyncPrinter.error("Invalid player turn data.");
            }
        });

        // Board Update (Opponent Move) Handler
        handlers.put(GameResponse.TYPE_BOARD_UPDATE, (resp, state) -> {
            if (resp.getData() instanceof BoardUpdate) {
                BoardUpdate data = (BoardUpdate) resp.getData();
                MoveHandler handler = new MoveHandler(state.getBoard());

                handler.makeMove(MoveConverter.fromJSON(data.getMove()), state.getEnemyColor());
                SyncPrinter.detail("Opponent moved: " + data.getMove());
                BoardPrinter.printBoard(state.getBoard());
            } else {
                SyncPrinter.error("Invalid board update data.");
            }
        });
    }

    public void dispatch(GameResponse<?> response, ClientState state) {
        if (response.isError()) {
            SyncPrinter.error("Server error: " + response.getMessage());
            return;
        }

        ResponseHandler handler = handlers.get(response.getType());
        if (handler != null) {
            handler.handle(response, state);
        } else {
            SyncPrinter.detail("Unknown response type: " + response.getType());
        }
    }
}