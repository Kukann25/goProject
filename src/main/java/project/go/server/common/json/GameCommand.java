package project.go.server.common.json;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

// JSON structure for game commands sent by clients
// Usage:
//
// GameCommand<?> cmd = JsonFmt.fromJson(jsonString, GameCommand.class);
// Object payload = cmd.getPayload();
// if (payload instanceof GameCommand.PayloadMakeMove) {
//    ...
// } 
// etc.
public class GameCommand<T> {
    static public class PayloadMakeMove {
        private String move;

        public PayloadMakeMove() {
        }

        public PayloadMakeMove(String move) {
            this.move = move;
        }

        public String getMove() {
            return move;
        }
    }

    public static final String COMMAND_MAKE_MOVE = "make-move";
    public static final String COMMAND_PASS = "pass";
    public static final String COMMAND_RESIGN = "resign";


    private String command;
    private String playerid;

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "command", // determined by the "command" field
        visible = true // keep "command" field visible for deserialization
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = PayloadMakeMove.class, name = COMMAND_MAKE_MOVE),
        @JsonSubTypes.Type(value = Object.class, name = COMMAND_PASS), // no payload
        @JsonSubTypes.Type(value = Object.class, name = COMMAND_RESIGN) // no payload
    })
    private T payload;

    public GameCommand() {
    }

    public GameCommand(String command, String playerid, T payload) {
        this.command = command;
        this.playerid = playerid;
        this.payload = payload;
    }

    public String getCommand() {
        return command;
    }

    public String getPlayerid() {
        return playerid;
    }

    public T getPayload() {
        return payload;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setPlayerid(String playerid) {
        this.playerid = playerid;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }
}

