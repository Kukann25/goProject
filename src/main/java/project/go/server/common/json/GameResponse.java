package project.go.server.common.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import project.go.applogic.Color;


public class GameResponse<T> {

    public static class BoardUpdate {
        private String move;

        public BoardUpdate() {
        }

        public BoardUpdate(String move) {
            this.move = move;
        }

        public String getMove() {
            return move;
        }

        public void setMove(String move) {
            this.move = move;
        }
    }

    public static class PlayerTurn {
        public static final String SIDE_BLACK = "black";
        public static final String SIDE_WHITE = "white";
        private String side;

        public PlayerTurn() {
        }

        public PlayerTurn(Color color) {
            if (color == Color.BLACK) {
                this.side = SIDE_BLACK;
            } else if (color == Color.WHITE) {
                this.side = SIDE_WHITE;
            } else {
                this.side = "";
            }
        }

        public PlayerTurn(String side) {
            this.side = side;
        }

        public String getSide() {
            return side;
        }

        public void setSide(String side) {
            this.side = side;
        }

        @JsonIgnore
        public Color getColor() {
            if (side.equals("black")) {
                return Color.BLACK;
            } else if (side.equals("white")) {
                return Color.WHITE;
            } else {
                return null;
            }
        }
    }

    // Data structure for match end information
    public static class MatchEnd {
        public static final String REASON_FORFEIT = "forfeit";
        public static final String REASON_NORMAL = "normal";
        public static final String REASON_ERROR = "error";

        public static final String WINNER_BLACK = "black";
        public static final String WINNER_WHITE = "white";
        public static final String WINNER_NONE = "none"; // for draw / error

        private String reason; // e.g., "forfeit", "normal" or "error"
        private String winner = WINNER_NONE; // "black", "white", or "none" for draw

        public MatchEnd() {}
        public MatchEnd(String reason, String winner) {
            this.reason = reason;
            this.winner = winner;
        }

        public String getReason() { return reason; }
        public String getWinner() { return winner; }
        public void setReason(String reason) { this.reason = reason; }
        public void setWinner(String winner) { this.winner = winner; }

        @JsonIgnore
        public Color getWinnerColor() {
            if (winner.equals(WINNER_BLACK)) {
                return Color.BLACK;
            } else if (winner.equals(WINNER_WHITE)) {
                return Color.WHITE;
            }
            return null;
        }

        @JsonIgnore
        public boolean isError() {
            return reason.equals(REASON_ERROR);
        }

        @JsonIgnore
        public boolean isForfeit() {
            return reason.equals(REASON_FORFEIT);
        }

        @JsonIgnore
        public boolean isNormalEnd() {
            return reason.equals(REASON_NORMAL);
        }
    }

    private int status; // First bit OK/Error, other bits verbose flags
    private String type;
    private String message;

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "type",
        visible = true // keep "command" field visible for deserialization
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Object.class, name = TYPE_STATUS),
        @JsonSubTypes.Type(value = Object.class, name = TYPE_PASS_DECLIEND),
        @JsonSubTypes.Type(value = Object.class, name = TYPE_PASS_MOVE),
        @JsonSubTypes.Type(value = BoardUpdate.class, name = TYPE_BOARD_UPDATE),
        @JsonSubTypes.Type(value = BoardUpdate.class, name = TYPE_VALID_MOVE),
        @JsonSubTypes.Type(value = PlayerTurn.class, name = TYPE_PLAYER_TURN),
        @JsonSubTypes.Type(value = MatchEnd.class, name = TYPE_MATCH_END),
    })
    private T data;

    public final static int STATUS_OK = 0;
    public final static int STATUS_ERROR = 1;
    public final static int STATUS_FATAL = 2; // unrecoverable error

    public final static int VERBOSE_VALID_MOVE = 1;
    public final static int VERBOSE_INVALID_MOVE = 2;
    public final static int VERBOSE_NOT_YOUR_TURN = 4;
    public final static int VERBOSE_INTERNAL_ERROR = 8;
    public final static int VERBOSE_MATCH_ENDED = 16;
    public final static int VERBOSE_UNKNOWN_COMMAND = 32;


    public final static String TYPE_STATUS = "status"; // no data, simply a status message
    public final static String TYPE_PLAYER_TURN = "player_turn"; // data contains PlayerTurn
    public final static String TYPE_BOARD_UPDATE = "board_update"; // data contains board state
    public final static String TYPE_VALID_MOVE = "valid_move"; // data contains move info
    public final static String TYPE_MATCH_END = "match_end"; // data contains match end info
    public final static String TYPE_PASS_MOVE = "pass_move"; // both sides deciede to pass
    public final static String TYPE_PASS_DECLIEND = "pass_move_declined"; // pass move was declined (game should resume)

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
        this.type = TYPE_STATUS;
        this.data = null;
    }

    public GameResponse(int status, String type, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.type = type;
    }

    /* Required by Jackson */
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public void setStatus(int status) { this.status = status; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @JsonIgnore
    public boolean isOk() {
        return (status & 1) == STATUS_OK;
    }

    @JsonIgnore
    public boolean isError() {
        return (status & 1) == STATUS_ERROR;
    }

    @JsonIgnore
    public int getVerboseFlags() {
        return (status & ~1) >> 1;
    }
}
