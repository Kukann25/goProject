package project.go.server.backend;

import project.go.server.common.json.GameCommand;
import project.go.server.common.json.GameResponse;

public interface MatchRequestHandler {
    GameResponse<?> handle(GameCommand<?> request, SharedMatchLogicState sharedState, Match.ClientData client);
}
