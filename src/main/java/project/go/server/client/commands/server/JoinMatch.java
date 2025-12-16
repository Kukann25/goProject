package project.go.server.client.commands.server;

import project.go.server.common.json.Connection;
import project.go.server.common.json.JsonFmt;
import project.go.server.common.json.PlayerTurn;
import project.go.server.client.BaseCommand;
import project.go.server.client.ClientConn;
import project.go.server.client.ServerCommand;
import project.go.server.client.SyncPrinter;

public class JoinMatch extends BaseCommand implements ServerCommand {
    public JoinMatch(String[] args) {
        super(args);
    }

    @Override
    public void execute(ClientConn connData) {
        try {
            // First, connect to the server
            connData.connect();
            String response = connData.receive();

            // Should be parsable as JSON
            Connection connection = JsonFmt.fromJson(response, Connection.class);
            connData.setConnection(connection);

            // Log the assigned clientId
            SyncPrinter.info("Connected to server: " + connData.getClientId());

            // Wait for the server to send match join confirmation
            response = connData.receive();
            PlayerTurn playerTurn = JsonFmt.fromJson(response, PlayerTurn.class);
            
            // Log the assigned side
            SyncPrinter.success("Joined match, your side: " + playerTurn.getSide());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
