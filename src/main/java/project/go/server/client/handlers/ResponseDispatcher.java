package project.go.server.client.handlers;

import java.util.HashMap;
import java.util.Map;

import project.go.server.client.ClientState;
import project.go.server.client.SyncPrinter;
import project.go.server.common.json.GameResponse;

public class ResponseDispatcher {
    private final Map<String, ResponseHandler> handlers = new HashMap<>();

    // Register a new response handler
    public void register(String responseType, ResponseHandler handler) {
        handlers.put(responseType, handler);
    }

    public void dispatch(GameResponse<?> response, ClientState state) {
        ResponseHandler handler = handlers.get(response.getType());
        if (handler != null) {
            handler.handle(response, state);
        } else {
            SyncPrinter.detail("Unknown response type: " + response.getType());
        }
    }
}