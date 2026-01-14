package project.go.server.client.views;

import javafx.application.Platform;
import javafx.scene.layout.GridPane;
import project.go.applogic.MoveConverter;
import project.go.applogic.MoveHandler;
import project.go.server.client.Client;
import project.go.server.client.ClientListener;
import project.go.server.client.CommandMatcher;
import project.go.server.client.SyncPrinter;
import project.go.server.client.components.BoardComponent;
import project.go.server.client.components.PlayerStats;
import project.go.server.client.handlers.ResponseDispatcher;
import project.go.server.common.json.GameResponse;
import project.go.server.common.json.GameResponse.BoardUpdate;
import project.go.server.client.components.Status;

public class Game extends GridPane {
    private BoardComponent board;
    private ResponseDispatcher dispatcher;
    private Status statusComponent;

    public static class Props {
        private GameResponse.PlayerTurn playerTurn;
        
        public Props(GameResponse.PlayerTurn playerTurn) {
            this.playerTurn = playerTurn;
        }

        public GameResponse.PlayerTurn getPlayerTurn() {
            return playerTurn;
        }
    }

    public Game(Props props) {
        this.statusComponent = new Status();
        statusComponent.setStatusText("Joined match, your side: " + props.playerTurn.getSide(), Status.StatusType.OK);

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
                    SyncPrinter.info(resp.getMessage());
                } else {
                    SyncPrinter.error("[Game] Server error: " + resp.getMessage());
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
                    statusComponent.setStatusText("Move accepted: " + data.getMove(), Status.StatusType.OK);
                    SyncPrinter.success("Move accepted: " + data.getMove());
                } else {
                    statusComponent.setStatusText(resp.getMessage(), Status.StatusType.ERROR);
                    SyncPrinter.error("Invalid move: " + resp.getMessage());
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
                    statusComponent.setStatusText("Opponent moved: " + data.getMove(), Status.StatusType.INFO);
                    SyncPrinter.info("Opponent moved: " + data.getMove());
                } else {
                    SyncPrinter.error("Invalid board update data received from server.");
                    statusComponent.setStatusText("Invalid board update data.", Status.StatusType.ERROR);
                }
            });
        });

        // Setup listener
        ClientListener listener = ClientListener.getInstance();
        listener.setDispatcher(this.dispatcher);

        this.add(new PlayerStats(props.getPlayerTurn().getColor()), 1, 0);
        this.add(board, 0, 0);

        // Make the status bar span two columns
        GridPane.setColumnSpan(statusComponent, 2);
        this.add(statusComponent, 0, 1);
    }
}
