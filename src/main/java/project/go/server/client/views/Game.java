package project.go.server.client.views;

import javafx.scene.layout.GridPane;
import project.go.applogic.MoveConverter;
import project.go.server.client.Client;
import project.go.server.client.ClientListener;
import project.go.server.client.CommandMatcher;
import project.go.server.client.components.BoardComponent;

public class Game extends GridPane {
    private BoardComponent board;
    private Thread listenerThread;

    public Game() {
        this.board = new BoardComponent(Client.getInstance().getClientState().getBoard());
        this.board.setCallback((point) -> {
            try {
                CommandMatcher.getServerCommand(
                "make-move", new String[]{MoveConverter.toJSON(point)})
                .execute(Client.getInstance().getConnection());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Setup listener
        listenerThread = new Thread(
            new ClientListener(Client.getInstance().getClientState(),
                            Client.getInstance().getConnection()));
        listenerThread.start();

        this.setPrefSize(800, 600);
        this.add(board, 0, 0);
    }
}
