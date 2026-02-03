package project.go.server.client;

public enum Path {
    HOME("home"),
    LOBBY("lobby"),
    GAME("game"),
    GAME_HISTORY("game_history"),
    GAME_HISTORY_LIST("game_history_list");

    private final String name;

    Path(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }
}
