package project.go.server.common.json;

public class GameModeRequest {
    public static final String MODE_PVP = "pvp";
    public static final String MODE_BOT = "bot";

    private String mode;
    private String clientId;

    public GameModeRequest() {
    }

    public GameModeRequest(String mode, String clientId) {
        this.mode = mode;
        this.clientId = clientId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
