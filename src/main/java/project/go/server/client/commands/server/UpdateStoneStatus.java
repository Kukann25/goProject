package project.go.server.client.commands.server;

import project.go.server.common.json.GameCommand;
import project.go.server.common.json.JsonFmt;
import project.go.server.client.BaseCommand;
import project.go.server.client.ClientConn;
import project.go.server.client.ServerCommand;

public class UpdateStoneStatus extends BaseCommand implements ServerCommand {
    public UpdateStoneStatus(String[] args) {
        super(args);
    }

    @Override
    public void execute(ClientConn connData) {
        try {
            if (args.length < 2) {
                return;
            }

            String position = args[0];
            String status = args[1];
            GameCommand<?> statusCommand = 
                new GameCommand<GameCommand.ChangeStoneStatus>(
                    GameCommand.COMMAND_CHANGE_STONE_STATUS,
                    connData.getClientId(),
                    new GameCommand.ChangeStoneStatus(position, status)
                );
            connData.send(JsonFmt.toJson(statusCommand));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
