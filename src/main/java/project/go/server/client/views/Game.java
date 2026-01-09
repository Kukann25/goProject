package project.go.server.client.views;

import javafx.scene.layout.GridPane;
import project.go.server.client.Client;
import project.go.server.client.components.BoardComponent;

public class Game extends GridPane {
    private BoardComponent board;

    public Game() {
        this.board = new BoardComponent(Client.getInstance().getClientState().getBoard());

        this.setPrefSize(800, 600);
        this.add(board, 0, 0);
    }
}
