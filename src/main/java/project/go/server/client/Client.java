package project.go.server.client;

import java.util.Scanner;

import project.go.Config;

public class Client {

    public static class ServerCommandRunner implements Runnable {
        private ServerCommand command;
        private ClientConn connection;

        public ServerCommandRunner(ServerCommand comm, ClientConn connection) {
            this.command = comm;
            this.connection = connection;
        }

        @Override
        public void run() {
             command.execute(connection);
        }
    }

    private ClientConn connection;
    private Thread commandThread;
    private ClientState clientState;

    public Client() {
        this(Config.PORT);
    }

    public Client(int port) {
        this.connection = new ClientConn(port);
        this.clientState = new ClientState();
    }

    public void run() {
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
                handleLocal(commandName, args);
                continue;
            } else if (CommandMatcher.isServerCommand(commandName)) {
                commandThread = new Thread(
                    new ServerCommandRunner(
                        CommandMatcher.getServerCommand(commandName, args), connection));
                commandThread.start();
            } else {
                SyncPrinter.error("Unknown command: " + commandName);
                SyncPrinter.detail("Type 'help' to see the list of available commands.");
            }
        }

        // Clean up
        connection.close();
        scanner.close();

        if (commandThread != null && commandThread.isAlive()) {
            try {
                commandThread.interrupt();
                commandThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        SyncPrinter.detail("Client terminated.");
    }

    private void handleLocal(String commandName, String[] args) {
        LocalCommand localCommand = CommandMatcher.getLocalCommand(commandName, args);
        if (localCommand != null) {
            localCommand.execute(clientState);
        } else {
            SyncPrinter.error("Failed to execute local command: " + commandName);
        }
    }
}
