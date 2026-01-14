package project.go.server.backend;

import java.util.HashMap;
import java.util.Map;

import project.go.server.common.json.GameCommand;
import project.go.server.common.json.GameResponse;
import project.go.server.common.json.StatusGameResponse;

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
