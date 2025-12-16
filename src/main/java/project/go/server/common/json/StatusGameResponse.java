package project.go.server.common.json;

public class StatusGameResponse extends GameResponse<Object> {
    public StatusGameResponse(int status, String message) {
        super(status, TYPE_STATUS, message, null);
    }

    public StatusGameResponse() {
        super();
    }
}
