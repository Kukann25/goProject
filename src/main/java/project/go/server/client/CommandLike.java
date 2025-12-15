package project.go.server.client;

public interface CommandLike {
    void execute(ClientConn connData);
}
