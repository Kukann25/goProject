package project.go.server.common.json;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GameResponse {
    private int status;
    private String message;

    public final static int STATUS_OK = 0;
    public final static int STATUS_ERROR = 1;

    public final static String MESSAGE_MOVE_OK = "Move accepted";
    public final static String MESSAGE_INVALID_MOVE = "Invalid move";
    public final static String MESSAGE_NOT_YOUR_TURN = "Not your turn";
    public final static String MESSAGE_INTERNAL_ERROR = "Internal error";
    public final static String MESSAGE_MATCH_ENDED = "Match ended";
    public final static String MESSAGE_UNKNOWN_COMMAND = "Unknown command";

    public GameResponse() {}
    public GameResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    /* Required by Jackson */
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public void setStatus(int status) { this.status = status; }
    public void setMessage(String message) { this.message = message; }

    @JsonIgnore
    public boolean isOk() {
        return status == STATUS_OK;
    }

    @JsonIgnore
    public boolean isError() {
        return status == STATUS_ERROR;
    }
}
