package project.go.server.client.commands.server;

import project.go.server.common.json.JsonFmt;
import project.go.server.common.json.GameCommand;
import project.go.server.client.BaseCommand;
import project.go.server.client.ClientConn;
import project.go.server.client.ServerCommand;

public class Resign extends BaseCommand implements ServerCommand {
    public Resign(String[] args) {
        super(args);
    }

    @Override
    public void execute(ClientConn connData) {
        try {
            GameCommand<?> resignCommand = new GameCommand<Object>(
                GameCommand.COMMAND_RESIGN, connData.getClientId(), null);

            // Send the resign command to the server
            String jsonCommand = JsonFmt.toJson(resignCommand);
            connData.send(jsonCommand);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
