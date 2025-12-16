package project.go.server.client;

public interface ServerCommand {
    void execute(ClientConn connData);
}
