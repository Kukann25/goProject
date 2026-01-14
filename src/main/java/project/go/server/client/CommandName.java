package project.go.server.client;

import project.go.server.common.json.GameCommand;

public enum CommandName {
    // Local commands
    EXIT("exit", "Exit the client application"),
    QUIT("quit"),
    HELP("help", "Display this help message"),

    // Server commands
    JOIN_MATCH("join-match", "Join a game match on the server"),
    DISCONNECT("disconnect", "Disconnect from the server"),
    MAKE_MOVE(GameCommand.COMMAND_MAKE_MOVE, "Make a move at specified coordinates. Usage: make-move <xx><yy> (e.g., make-move 0312 for x=3, y=12)"),
    RESIGN(GameCommand.COMMAND_RESIGN, "Resign from the current match"),
    PASS(GameCommand.COMMAND_PASS, "Pass your turn in the current match"),
    UPDATE_STONE_STATUS(GameCommand.COMMAND_CHANGE_STONE_STATUS, "Update the status of stones on the board");

    private final String commandName;
    private final String description;


    CommandName(String commandName) {
        this.commandName = commandName;
        this.description = "";
    }

    CommandName(String commandName, String description) {
        this.commandName = commandName;
        this.description = description;
    }

    /**
     * Get the descriptive name of the command
     */
    public String getCommandName() {
        return commandName;
    }

    /**
     * Get the command description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Convert string to CommandName enum
     */
    static public CommandName fromString(String commandName) {
        for (CommandName type : CommandName.values()) {
            if (type.getCommandName().equals(commandName)) {
                return type;
            }
        }
        return null;
    }
}
