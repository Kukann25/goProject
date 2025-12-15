package project.go.server.client;

public abstract class BaseCommand implements CommandLike {
    protected String[] args;

    public BaseCommand(String[] args) {
        this.args = args;
    }
}
