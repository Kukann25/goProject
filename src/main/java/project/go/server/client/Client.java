package project.go.server.client;

import java.util.Scanner;

public class Client {
    private ClientConn connection;

    public Client(int port) {
        this.connection = new ClientConn(port);
    }

    static private void log(String message) {
        System.out.print(message);
    }

    public void run() {
        log("GoProject\n");
        Scanner scanner = new Scanner(System.in);


        while(true) {
            log("> ");
            String line = scanner.nextLine();

            if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                log("Exiting...\n");
                break;
            }

            processCommand(line);
        }

        System.out.println("Client terminated.");
        // Clean up
        scanner.close();
        connection.close();
    }

    private void processCommand(String line) {
        String[] parts = line.split(" ");
        String commandName = parts[0];
        String[] args = null;

        if (parts.length > 1) {
            args = new String[parts.length - 1];
            System.arraycopy(parts, 1, args, 0, args.length);
        }

        CommandLike command = CommandMatcher.matchCommand(commandName, args);
        if (command == null) {
            log("Unknown command: " + commandName + "\n");
            return;
        }

        command.execute(connection);
    }
}
