package project.go.server.client.commands.server;

import project.go.server.common.json.JsonFmt;
import project.go.server.common.json.GameCommand;
import project.go.server.client.BaseCommand;
import project.go.server.client.ClientConn;
import project.go.server.client.ServerCommand;

public class PassMove extends BaseCommand implements ServerCommand {
    public PassMove(String[] args) {
        super(args);
    }

    @Override
    public void execute(ClientConn connData) {
        try {
            GameCommand<?> moveCommand = new GameCommand<Object>(
                GameCommand.COMMAND_PASS, connData.getClientId(), null);

            // Send the move to the server
            String jsonCommand = JsonFmt.toJson(moveCommand);
            connData.send(jsonCommand);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}