package project.go.server.client.commands.server;

import project.go.server.common.json.GameCommand;
import project.go.server.common.json.JsonFmt;
import project.go.server.client.BaseCommand;
import project.go.server.client.ClientConn;
import project.go.server.client.ServerCommand;

public class ResumeGame extends BaseCommand implements ServerCommand {
    public ResumeGame(String[] args) {
        super(args);
    }

    @Override
    public void execute(ClientConn connData) {
        try {
            GameCommand<?> cmd = new GameCommand<Object>(
                GameCommand.COMMAND_RESUME,
                connData.getClientId(),
                null
            );
            connData.send(JsonFmt.toJson(cmd));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
