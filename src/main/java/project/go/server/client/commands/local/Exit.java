package project.go.server.client.commands.local;

import project.go.server.client.BaseCommand;
import project.go.server.client.ClientState;
import project.go.server.client.LocalCommand;

public class Exit extends BaseCommand implements LocalCommand {
    public Exit(String[] args) {
        super(args);
    }

    @Override
    public void execute(ClientState clientState) {
        System.out.println("Exiting client application.");
        clientState.stop();
    }
}
