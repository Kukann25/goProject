package project.go.server.client.commands.server;

import project.go.server.client.BaseCommand;
import project.go.server.client.ClientConn;
import project.go.server.client.ServerCommand;

public class JoinMatch extends BaseCommand implements ServerCommand {
    public JoinMatch(String[] args) {
        super(args);
    }

    @Override
    public void execute(ClientConn connData) {
        try {
            connData.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
