package project.go.server.common.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import project.go.applogic.StoneStatus;

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

    static public class ChangeStoneStatus {
        public static final String STATUS_ALIVE = "alive";
        public static final String STATUS_DEAD = "dead";

        private String position;
        private String status;

        public ChangeStoneStatus() {
        }

        public ChangeStoneStatus(String position, String status) {
            this.position = position;
            this.status = status;
        }

        public String getPosition() {
            return position;
        }

        public String getStatus() {
            return status;
        }

        @JsonIgnore
        public StoneStatus toStoneStatus() {
            if (STATUS_ALIVE.equals(this.status)) {
                return StoneStatus.ALIVE;
            } else if (STATUS_DEAD.equals(this.status)) {
                return StoneStatus.DEAD;
            } else {
                return StoneStatus.UNKNOWN;
            }
        }
    }

    public static final String COMMAND_MAKE_MOVE = "make-move";
    public static final String COMMAND_PASS = "pass";
    public static final String COMMAND_RESIGN = "resign";
    public static final String COMMAND_CHANGE_STONE_STATUS = "change-stone-status";


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
        @JsonSubTypes.Type(value = ChangeStoneStatus.class, name = COMMAND_CHANGE_STONE_STATUS),
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

