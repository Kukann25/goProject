package project.go.server.client.commands.local;

import project.go.server.client.BaseCommand;
import project.go.server.client.ClientState;
import project.go.server.client.CommandName;
import project.go.server.client.LocalCommand;
import project.go.server.client.SyncPrinter;

public class Help extends BaseCommand implements LocalCommand {
    
    public Help(String[] args) {
        super(args);
    }

    @Override
    public void execute(ClientState clientState) {
        SyncPrinter.detail("Available commands:");
        for (CommandName cmdName : CommandName.values()) {
            String desc = cmdName.getDescription();
            if (desc != null) {
                SyncPrinter.detail(" - " + cmdName.getCommandName() + ": " + desc);
            } else {
                SyncPrinter.detail(" - " + cmdName.getCommandName());
            }
        }
    }
}
