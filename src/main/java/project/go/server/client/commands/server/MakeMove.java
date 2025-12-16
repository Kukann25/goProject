package project.go.server.client.commands.server;

import project.go.server.common.json.JsonFmt;
import project.go.server.common.json.GameCommand;
import project.go.server.common.json.GameResponse;
import project.go.server.client.BaseCommand;
import project.go.server.client.ClientConn;
import project.go.server.client.ServerCommand;
import project.go.server.client.SyncPrinter;

public class MakeMove extends BaseCommand implements ServerCommand {
    public MakeMove(String[] args) {
        super(args);
    }

    @Override
    public void execute(ClientConn connData) {
        try {
            // The move should be in args[0] as "xxYY" where xx is the x coordinate and YY is the y coordinate
            if (args.length != 1) {
                SyncPrinter.detail("Usage: makemove <xx><yy>");
                return;
            }

            String moveStr = args[0];
            GameCommand<GameCommand.PayloadMakeMove> moveCommand = new GameCommand<>(
                GameCommand.COMMAND_MAKE_MOVE, connData.getClientId(), new GameCommand.PayloadMakeMove(moveStr));

            // Send the move to the server
            String jsonCommand = JsonFmt.toJson(moveCommand);
            connData.send(jsonCommand);

            // Get the server response
            String response = connData.receive();
            GameResponse gameResp = JsonFmt.fromJson(response, GameResponse.class);
            if (gameResp.isError()) {
                SyncPrinter.error("Error from server: " + gameResp.getMessage());
            } else {
                SyncPrinter.success("Move accepted");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
