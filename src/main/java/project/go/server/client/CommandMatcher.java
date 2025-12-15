package project.go.server.client;

import project.go.server.client.commands.JoinMatch;
import project.go.server.client.commands.MakeMove;

public class CommandMatcher {
    private CommandMatcher() {
        // no instances
    }

    public static CommandLike matchCommand(String name, String[] args) {
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
}
