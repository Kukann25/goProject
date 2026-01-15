package project.go.server.backend;

import java.util.HashMap;
import java.util.Map;

import project.go.server.common.json.GameCommand;
import project.go.server.common.json.GameResponse;
import project.go.server.common.json.StatusGameResponse;
import project.go.applogic.StoneStatus;

import project.go.server.common.json.GameCommand.ChangeStoneStatus;

public class MatchRequestDispatcher {
    public final Map<String, MatchRequestHandler> handlers = new HashMap<>();
    private final SharedMatchLogicState sharedState;
    private final Match.ClientData playerData;

    public MatchRequestDispatcher(SharedMatchLogicState sharedState, Match.ClientData playerData) {
        this.sharedState = sharedState;
        this.playerData = playerData;
        registerHandlers();
    }


    private static void log(String msg) {
        Logger.getInstance().log("MatchRequestDispatcher", msg);
    }

    private void registerHandlers() {
        
        // Register Move Handler
        handlers.put(GameCommand.COMMAND_MAKE_MOVE, (request, sharedState, client) -> {
            Object payload = request.getPayload();
            if (!(payload instanceof GameCommand.PayloadMakeMove)) {
                return new GameResponse<>(
                    GameResponse.STATUS_ERROR,
                    GameResponse.TYPE_STATUS,
                    "Invalid payload for make-move command",
                    null
                );
            }

            GameCommand.PayloadMakeMove movePayload = (GameCommand.PayloadMakeMove) payload;
            try {
                // Validate and apply the move using shared state
                sharedState.makeMove(movePayload.getMove(), client.getSide());
                log("Player " + client.data().getClientId() + " played move: " + movePayload.getMove());
                return new GameResponse<GameResponse.BoardUpdate>(
                    StatusGameResponse.STATUS_OK,
                    GameResponse.TYPE_VALID_MOVE,
                    StatusGameResponse.MESSAGE_MOVE_OK,
                    new GameResponse.BoardUpdate(movePayload.getMove()));
            }  catch (IllegalArgumentException e) {
                log("Illegal move from player " + client.data().getClientId() + ": " + e.getMessage());
                return new StatusGameResponse(StatusGameResponse.STATUS_ERROR, StatusGameResponse.MESSAGE_INVALID_MOVE);
            }
        });

        handlers.put(GameCommand.COMMAND_PASS, (request, sharedState, client) -> {
            try {
                sharedState.passTurn(client.getSide());
                log("Player " + client.data().getClientId() + " passed their turn.");
                return new GameResponse<GameResponse.BoardUpdate>(
                    StatusGameResponse.STATUS_OK,
                    GameResponse.TYPE_VALID_MOVE,
                    StatusGameResponse.MESSAGE_MOVE_OK,
                    new GameResponse.BoardUpdate("pass"));
            } catch (IllegalArgumentException e) {
                log("Invalid pass from player " + client.data().getClientId() + ": " + e.getMessage());
                return new StatusGameResponse(StatusGameResponse.STATUS_ERROR, "Invalid pass move.");
            }
        });

        handlers.put(GameCommand.COMMAND_RESIGN, (request, sharedState, client) -> {
            try {
                sharedState.resign(client.getSide());
                log("Player " + client.data().getClientId() + " resigned the match.");
                log("Shared state: " + sharedState.getMatchState().toString());

                return new StatusGameResponse(StatusGameResponse.STATUS_OK, "Resignation accepted.");
            } catch (IllegalArgumentException e) {
                log("Invalid resignation from player " + client.data().getClientId() + ": " + e.getMessage());
                return new StatusGameResponse(StatusGameResponse.STATUS_ERROR, "Invalid resignation.");
            }
        });

        handlers.put(GameCommand.COMMAND_RESUME, (request, sharedState, client) -> {
            sharedState.resumeGame(client.getSide());
            log("Player " + client.data().getClientId() + " resumed the game.");
            return new GameResponse<>(
                StatusGameResponse.STATUS_OK,
                GameResponse.TYPE_GAME_RESUMED,
                GameResponse.MESSAGE_GAME_RESUMED,
                null
            );
        });

        handlers.put(GameCommand.COMMAND_CHANGE_STONE_STATUS, (request, sharedState, client) -> {
            Object payload = request.getPayload();
            if (!(payload instanceof ChangeStoneStatus)) {
                return new StatusGameResponse(StatusGameResponse.STATUS_ERROR, "Invalid payload for stone status change");
            }
            ChangeStoneStatus statusPayload = (ChangeStoneStatus) payload;
            String pos = statusPayload.getPosition();
            
            if ("all".equalsIgnoreCase(pos)) {
                sharedState.updateStatus(client.getSide(), -1, -1, StoneStatus.ALIVE);
                if (sharedState.checkAgreement()) {
                    log("Players agreed on stone status (via all-alive). Ending match.");
                    sharedState.getMatchState().addState(MatchState.COMPLETED);
                }
                return new StatusGameResponse(StatusGameResponse.STATUS_OK, "All stones marked alive");
            }

            String[] parts = pos.split("-");
            if (parts.length != 2) {
                return new StatusGameResponse(StatusGameResponse.STATUS_ERROR, "Invalid position format");
            }

            try {
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                
                sharedState.updateStatus(client.getSide(), x, y, statusPayload.toStoneStatus());
                
                if (sharedState.checkAgreement()) {
                    log("Players agreed on stone status. Ending match.");
                    sharedState.getMatchState().addState(MatchState.COMPLETED);
                }

                return new StatusGameResponse(StatusGameResponse.STATUS_OK, "Status updated");
            } catch (NumberFormatException e) {
                return new StatusGameResponse(StatusGameResponse.STATUS_ERROR, "Invalid coordinates");
            }
        });
    }

    public GameResponse<?> dispatch(GameCommand<?> request) {
        MatchRequestHandler handler = handlers.get(request.getCommand());
        if (handler != null) {
            return handler.handle(request, sharedState, playerData);
        } else {
            return new GameResponse<>(
                GameResponse.STATUS_ERROR,
                GameResponse.TYPE_STATUS,
                "Unknown command type: " + request.getCommand(),
                null
            );
        }
    }
}
