package project.go.server.client;

import project.go.Config;
import project.go.applogic.Board;
import project.go.applogic.Color;

/**
 * Class to manage the state of the client application.
 */
public class ClientState {
    private boolean isRunning;
    private Board board;
    private Color playerColor;

    public ClientState() {
        this.isRunning = true;
        this.playerColor = Color.NONE;
        this.board = new Board(Config.DEFAULT_BOARD_SIZE);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public Board getBoard() {
        return board;
    }

    public void setPlayerColor(Color color) {
        this.playerColor = color;
    }

    public Color getPlayerColor() {
        return playerColor;
    }

    public Color getEnemyColor() {
        return playerColor == Color.BLACK ? Color.WHITE : Color.BLACK;
    }

    public void stop() {
        this.isRunning = false;
    }
}
