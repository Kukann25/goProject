package project.go.server.client.handlers;

import project.go.server.client.ClientState;
import project.go.server.common.json.GameResponse;

public interface IResponseDispatcher {
    public void dispatch(GameResponse<?> response, ClientState state);
}
