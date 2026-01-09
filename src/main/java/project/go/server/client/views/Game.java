package project.go.server.client.views;

import javafx.application.Platform;
import javafx.scene.layout.GridPane;
import project.go.applogic.MoveConverter;
import project.go.applogic.MoveHandler;
import project.go.server.client.Client;
import project.go.server.client.ClientListener;
import project.go.server.client.CommandMatcher;
import project.go.server.client.components.BoardComponent;
import project.go.server.client.handlers.ResponseDispatcher;
import project.go.server.common.json.GameResponse;
import project.go.server.common.json.GameResponse.BoardUpdate;
import project.go.server.client.components.Status;

public class Game extends GridPane {
    private BoardComponent board;
    private Thread listenerThread;
    private ResponseDispatcher dispatcher;
    private Status statusComponent;

    public Game() {
        this.statusComponent = new Status();
        this.statusComponent.setStatusText("Connected", Status.StatusType.OK);

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

        this.dispatcher = new ResponseDispatcher();

        // Status Handler
        this.dispatcher.register(GameResponse.TYPE_STATUS, (resp, state) -> {
            Platform.runLater(() -> {
                if (!resp.isError()) {
                    statusComponent.setStatusText(resp.getMessage(), Status.StatusType.INFO);
                } else {
                    statusComponent.setStatusText("Server error: " + resp.getMessage(), Status.StatusType.ERROR);
                }
            });
        });

        // Valid Move Handler (makes the move on client side if valid)
        this.dispatcher.register(GameResponse.TYPE_VALID_MOVE, (resp, state) -> {
            Platform.runLater(() -> {
                if (!resp.isError()) {
                    BoardUpdate data = (BoardUpdate) resp.getData();
                    MoveHandler handler = new MoveHandler(state.getBoard());
                    handler.makeMove(MoveConverter.fromJSON(data.getMove()), state.getPlayerColor());
                    board.update(state.getBoard());
                    statusComponent.setStatusText("Move accepted: " + resp.getMessage(), Status.StatusType.OK);
                } else {
                    statusComponent.setStatusText("Invalid move: " + resp.getMessage(), Status.StatusType.ERROR);
                }
            });
        });

        // Player Turn Handler
        this.dispatcher.register(GameResponse.TYPE_PLAYER_TURN, (resp, state) -> {
            Platform.runLater(() -> {
                if (resp.getData() instanceof GameResponse.PlayerTurn) {
                    GameResponse.PlayerTurn data = (GameResponse.PlayerTurn) resp.getData();
                    state.setPlayerColor(data.getColor());
                    statusComponent.setStatusText("Joined match, your side: " + data.getSide(), Status.StatusType.OK);
                } else {
                    statusComponent.setStatusText("Invalid player turn data.", Status.StatusType.ERROR);
                }
            });
        });

        // Board Update (Opponent Move) Handler
        this.dispatcher.register(GameResponse.TYPE_BOARD_UPDATE, (resp, state) -> {
            Platform.runLater(() -> {
                if (resp.getData() instanceof GameResponse.BoardUpdate) {
                    GameResponse.BoardUpdate data = (GameResponse.BoardUpdate) resp.getData();
                    MoveHandler handler = new MoveHandler(state.getBoard());
                    // Make opponent move
                    handler.makeMove(MoveConverter.fromJSON(data.getMove()), state.getEnemyColor());
                    board.update(state.getBoard());
                    statusComponent.setStatusText("Opponent moved: " + resp.getMessage(), Status.StatusType.INFO);
                    
                } else {
                    statusComponent.setStatusText("Invalid board update data.", Status.StatusType.ERROR);
                }
            });
        });

        // Setup listener

        ClientListener listener = new ClientListener(
            Client.getInstance().getClientState(),
            Client.getInstance().getConnection()
        );
        listener.setDispatcher(this.dispatcher);

        listenerThread = new Thread(listener);
        listenerThread.start();
        

        this.add(board, 0, 0);
        this.add(statusComponent, 0, 1);
    }
}
