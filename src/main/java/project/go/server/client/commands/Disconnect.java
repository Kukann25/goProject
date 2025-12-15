package project.go.server.client.commands;

import project.go.server.client.BaseCommand;
import project.go.server.client.ClientConn;

public class Disconnect extends BaseCommand {
    public Disconnect(String[] args) {
        super(args);
    }

    @Override
    public void execute(ClientConn connData) {
        connData.close();
        System.out.println("Disconnected from server.");
    }
}
