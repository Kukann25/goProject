package project.go.server.client;

import project.go.server.client.commands.local.Exit;
import project.go.server.client.commands.local.Help;
import project.go.server.client.commands.server.JoinMatch;
import project.go.server.client.commands.server.MakeMove;

/**
 * Matches command names to their implementations
 */
public final class CommandMatcher {
    private CommandMatcher() {
        // no instances
    }

    public static boolean isLocalCommand(String name) {
        return getLocalCommand(name, null) != null;
    }

    public static boolean isServerCommand(String name) {
        return getServerCommand(name, null) != null;
    }

    /**
     * Get server command (the one that interacts with it)
     */
    public static ServerCommand getServerCommand(String name, String[] args) {
        CommandName commandName = CommandName.fromString(name);
        if (commandName == null) {
            return null;
        }

        switch (commandName) {
            case JOIN_MATCH:
                return new JoinMatch(args);
            case MAKE_MOVE:
                return new MakeMove(args);
            default:
                return null;
        }
    }

    /**
     * Get local command by name (the one that interacts with the client's state only)
     */
    public static LocalCommand getLocalCommand(String name, String[] args) {
        CommandName commandName = CommandName.fromString(name);
        if (commandName == null) {
            return null;
        }

        switch (commandName) {
            case EXIT:
            case QUIT:
                return new Exit(args);
            case HELP:
                return new Help(args);
            default:
                return null;
        }
    }
}
