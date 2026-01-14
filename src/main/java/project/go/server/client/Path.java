package project.go.server.client;

public enum Path {
    HOME("/fxml/home.fxml", "home"),
    LOBBY("/fxml/lobby.fxml", "lobby"),
    GAME("/fxml/game.fxml", "game");

    private final String path;
    private final String name;

    Path(String path, String name) {
        this.path = path;
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }
}
