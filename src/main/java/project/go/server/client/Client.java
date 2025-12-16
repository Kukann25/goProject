package project.go.server.client;

import java.util.Scanner;

import project.go.Config;

public class Client {
    private ClientConn connection;
    private Thread listenerThread;
    private ClientState clientState;

    public Client() {
        this(Config.PORT);
    }

    public Client(int port) {
        this.connection = new ClientConn(port);
        this.clientState = new ClientState();
    }

    public void run() {
        listenerThread = new Thread(new ClientListener(clientState, connection));
        listenerThread.start();

        SyncPrinter.println("GoProject\n");
        Scanner scanner = new Scanner(System.in);

        while(clientState.isRunning()) {
            String line = scanner.nextLine();
            String[] parts = line.split(" ");
            String commandName = parts[0];
            String[] args = null;

            if (parts.length > 1) {
                args = new String[parts.length - 1];
                System.arraycopy(parts, 1, args, 0, args.length);
            }

            if (CommandMatcher.isLocalCommand(commandName)) {
                CommandMatcher.getLocalCommand(commandName, args).execute(clientState);
                continue;
            } else if (CommandMatcher.isServerCommand(commandName)) {
                CommandMatcher.getServerCommand(commandName, args).execute(connection);
            } else {
                SyncPrinter.error("Unknown command: " + commandName);
                SyncPrinter.detail("Type 'help' to see the list of available commands.");
            }
        }

        // Clean up
        connection.close();
        scanner.close();

        SyncPrinter.detail("Client terminated.");
    }

    
}
