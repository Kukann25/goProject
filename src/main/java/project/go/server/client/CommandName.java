package project.go.server.client;

import project.go.server.common.json.GameCommand;

public enum CommandName {
    // Client (only) commands
    JOIN_MATCH("join-match"),
    DISCONNECT("disconnect"),

    // Server-client commands
    MAKE_MOVE(GameCommand.COMMAND_MAKE_MOVE),
    RESIGN(GameCommand.COMMAND_RESIGN),
    PASS(GameCommand.COMMAND_PASS);

    private final String commandName;

    CommandName(String commandName) {
        this.commandName = commandName;
    }

    /**
     * Get the descriptive name of the command
     */
    public String getCommandName() {
        return commandName;
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
