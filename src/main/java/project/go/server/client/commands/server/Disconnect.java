package project.go.server.client.commands.server;

import project.go.server.client.BaseCommand;
import project.go.server.client.ClientConn;
import project.go.server.client.ServerCommand;
import project.go.server.client.SyncPrinter;

public class Disconnect extends BaseCommand implements ServerCommand {
    public Disconnect(String[] args) {
        super(args);
    }

    @Override
    public void execute(ClientConn connData) {
        connData.close();
        SyncPrinter.success("Disconnected from server.");
    }
}
