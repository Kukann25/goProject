package project.go.server.client.views;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import project.go.applogic.MoveConverter;
import project.go.applogic.MoveHandler;
import project.go.server.client.Client;
import project.go.server.client.ClientListener;
import project.go.server.client.ClientListenerThread;
import project.go.server.client.CommandMatcher;
import project.go.server.client.Path;
import project.go.server.client.Router;
import project.go.server.client.SyncPrinter;
import project.go.server.client.components.BoardComponent;
import project.go.server.client.components.PlayerStats;
import project.go.server.client.handlers.ResponseDispatcher;
import project.go.server.common.json.GameCommand;
import project.go.server.common.json.GameResponse;
import project.go.server.common.json.GameResponse.BoardUpdate;
import project.go.server.client.components.Status;

public class Game extends GridPane {
    private Stage primaryStage;
    private BoardComponent board;
    private ResponseDispatcher dispatcher;
    private Status statusComponent;
    private Button resignButton;
    private Button passButton;

    public static class Props {
        private GameResponse.PlayerTurn playerTurn;
        
        public Props(GameResponse.PlayerTurn playerTurn) {
            this.playerTurn = playerTurn;
        }

        public GameResponse.PlayerTurn getPlayerTurn() {
            return playerTurn;
        }
    }

    public Game(Stage primaryStage, Props props) {
        this.primaryStage = primaryStage;
        this.statusComponent = new Status();
        statusComponent.setStatusText("Joined match, your side: " + props.playerTurn.getSide(), Status.StatusType.OK);

        this.board = new BoardComponent(Client.getInstance().getClientState().getBoard());
        this.board.setCallback((point) -> {
            try {
                CommandMatcher.getServerCommand(
                GameCommand.COMMAND_MAKE_MOVE, new String[]{MoveConverter.toJSON(point)})
                .execute(Client.getInstance().getConnection());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        this.passButton = new Button("Pass");
        this.resignButton = new Button("Resign");
        registerCallbacks();
        registerHandlers();
        
        // Setup listener
        ClientListener listener = ClientListenerThread.getInstance().getListener();
        listener.setDispatcher(this.dispatcher);

        // Add components to layout
        VBox rightPanel = new VBox(10, 
            new PlayerStats(props.getPlayerTurn().getColor()), 
            passButton, resignButton);
        rightPanel.setStyle("-fx-padding: 10;-fx-background-color: #e5c683ff;");
        
        this.add(rightPanel, 1, 0);
        this.add(board, 0, 0);

        // Make the status bar span two columns
        GridPane.setColumnSpan(statusComponent, 2);
        this.add(statusComponent, 0, 1);
    }

    private void registerCallbacks() {
        this.passButton.setOnAction(e -> {
            try {
                CommandMatcher.getServerCommand(
                    GameCommand.COMMAND_PASS, null).execute(
                        Client.getInstance().getConnection());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        this.resignButton.setOnAction(e -> {
            try {
                CommandMatcher.getServerCommand(
                    GameCommand.COMMAND_RESIGN, null).execute(
                        Client.getInstance().getConnection());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void registerHandlers() {
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

        // Pass accepted Handler (opponent also passed the turn - start negotiation)
        this.dispatcher.register(GameResponse.TYPE_PASS_MOVE, (resp, state) -> {
            Platform.runLater(() -> {
                statusComponent.setStatusText("Both players passed. Match end negotiation started.", Status.StatusType.INFO);
            });
        });

        // Match End Handler
        this.dispatcher.register(GameResponse.TYPE_MATCH_END, (resp, state) -> {
            Platform.runLater(() -> {
                if (resp.getData() instanceof GameResponse.MatchEnd) {
                    GameResponse.MatchEnd data = (GameResponse.MatchEnd) resp.getData();
                    String endMessage = "Match ended. Reason: " + data.getReason() + ". Winner: " + data.getWinner();
                    statusComponent.setStatusText(endMessage, Status.StatusType.INFO);
                    SyncPrinter.info(endMessage);

                    final Stage dialog = new Stage();
                    dialog.initModality(Modality.APPLICATION_MODAL);
                    dialog.initOwner(this.primaryStage);

                    project.go.applogic.Color myColor = state.getPlayerColor();
                    project.go.applogic.Color winnerColor = data.getWinnerColor();
                    String resultTitle = "Match Ended";
                    String outcomeText;
                    if (winnerColor == null) {
                        outcomeText = "Draw";
                    } else if (winnerColor == myColor) {
                        outcomeText = "You Won!";
                    } else {
                        outcomeText = "You Lost";
                    }

                    VBox dialogVbox = new VBox(20);
                    dialogVbox.setStyle("-fx-padding: 20; -fx-alignment: center;");
                    
                    Text header = new Text(outcomeText);
                    header.setStyle("-fx-font-size: 20pt; -fx-font-weight: bold;");

                    Text info = new Text("Reason: " + data.getReason() + "\nWinner: " + data.getWinner());
                    info.setStyle("-fx-text-alignment: center;");

                    Button okButton = new Button("OK");
                    okButton.setOnAction(e -> {
                        // Make a new close event to handle cleanup
                        dialog.fireEvent(new javafx.stage.WindowEvent(
                            dialog, javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST));
                    });

                    dialogVbox.getChildren().addAll(header, info, okButton);
                    Scene dialogScene = new Scene(dialogVbox, 400, 250);
                    dialog.setScene(dialogScene);
                    dialog.setTitle(resultTitle);

                    dialog.setOnShown(e -> {
                        dialog.setX(primaryStage.getX() + (primaryStage.getWidth() / 2) - (dialog.getWidth() / 2));
                        dialog.setY(primaryStage.getY() + (primaryStage.getHeight() / 2) - (dialog.getHeight() / 2));
                    });
                    
                    dialog.show();

                    dialog.setOnCloseRequest((e) -> {
                        ClientListenerThread.getInstance().kill();
                        Client.getInstance().reset();
                        Router.route(Path.HOME);
                    });
                } else {
                    SyncPrinter.error("Invalid match end data received from server.");
                    statusComponent.setStatusText("Invalid match end data.", Status.StatusType.ERROR);
                }
            });
        });
    }
}
