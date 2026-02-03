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
import project.go.applogic.StoneStatus;

public class Game extends GridPane {
    private Stage primaryStage;
    private BoardComponent board;
    private ResponseDispatcher dispatcher;
    private Status statusComponent;
    private Button resignButton;
    private Button passButton;
    private Button resumeButton;
    private Button allAliveButton;
    private project.go.applogic.Color myColor;

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
        this.myColor = props.playerTurn.getColor();
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
        this.resumeButton = new Button("Resume Game");
        this.allAliveButton = new Button("Suggest All Alive");
        
        // Hide negotiation buttons initially
        this.resumeButton.setVisible(false);
        this.resumeButton.setManaged(false);
        this.allAliveButton.setVisible(false);
        this.allAliveButton.setManaged(false);

        registerCallbacks();
        registerHandlers();
        
        // Setup listener
        ClientListener listener = ClientListenerThread.getInstance().getListener();
        listener.setDispatcher(this.dispatcher);

        // Add components to layout
        VBox rightPanel = new VBox(10, 
            new PlayerStats(props.getPlayerTurn().getColor()), 
            passButton, resignButton, resumeButton, allAliveButton);
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


        this.resumeButton.setOnAction(e -> {
            try {
                CommandMatcher.getServerCommand(
                    GameCommand.COMMAND_RESUME, null).execute(
                        Client.getInstance().getConnection());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        this.allAliveButton.setOnAction(e -> {
            try {
                CommandMatcher.getServerCommand(
                    GameCommand.COMMAND_CHANGE_STONE_STATUS, 
                    new String[]{"all", "alive"}
                ).execute(Client.getInstance().getConnection());
                
                // Optimistic visual update not easy for "ALL" without board iteration here
                // We rely on server response or local full redraw
                // Actually we can iterate over board and update local status
                // But server response is fast enough usually.
                // However, let's do it for responsiveness
                // But wait, BoardComponent logic for getting current board state to iterate is not exposed fully
                // (except via draw, but we need loop).
                // Let's just trust server.
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

                    if (data.getMove().equals("pass")) {
                        handler.pass(state.getPlayerColor());
                        statusComponent.setStatusText("Passed turn", Status.StatusType.INFO);
                        SyncPrinter.info("Passed turn");
                        return;
                    }

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

                    if (data.getMove().equals("pass")) {
                        handler.pass(state.getEnemyColor());
                        statusComponent.setStatusText("Opponent passed their turn.", Status.StatusType.INFO);
                        SyncPrinter.info("Opponent passed their turn.");
                        return;
                    }
                    
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

        // Pass move negotation (opponent also passed the turn - start negotiation)
        this.dispatcher.register(GameResponse.TYPE_PASS_MOVE, (resp, state) -> {
            Platform.runLater(() -> {
                statusComponent.setStatusText("Both players passed. Match end negotiation started. Click stones to mark dead/alive.", Status.StatusType.INFO);
                SyncPrinter.info("Both players passed. Match end negotiation started.");
                
                // Toggle buttons
                passButton.setVisible(false);
                passButton.setManaged(false);
                resumeButton.setVisible(true);
                resumeButton.setManaged(true);
                allAliveButton.setVisible(true);
                allAliveButton.setManaged(true);

                board.startNegotiation(state.getBoard());
                board.setCallback((point) -> {
                    StoneStatus current = board.getMyStatus(point.getX(), point.getY());
                    if (current == StoneStatus.NONE) return;

                    StoneStatus next;
                    if (current != StoneStatus.DEAD) next = StoneStatus.DEAD;
                    else next = StoneStatus.ALIVE;
                    
                    String statusStr = (next == StoneStatus.DEAD) ? "dead" : "alive";
                    String posStr = point.getX() + "-" + point.getY();

                    try {
                        CommandMatcher.getServerCommand(
                            GameCommand.COMMAND_CHANGE_STONE_STATUS, 
                            new String[]{posStr, statusStr}
                        ).execute(Client.getInstance().getConnection());
                        
                        // Optimistic update
                        board.updateStatus(point.getX(), point.getY(), next, true);
                        board.redraw();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            });
        });

        this.dispatcher.register(GameResponse.TYPE_GAME_RESUMED, (resp, state) -> {
            Platform.runLater(() -> {
                statusComponent.setStatusText(resp.getMessage(), Status.StatusType.INFO);
                SyncPrinter.info(resp.getMessage());

                // Toggle buttons back
                passButton.setVisible(true);
                passButton.setManaged(true);
                resumeButton.setVisible(false);
                resumeButton.setManaged(false);
                allAliveButton.setVisible(false);
                allAliveButton.setManaged(false);

                board.stopNegotiation();
                // Restore move making behavior
                board.setCallback((point) -> {
                    try {
                        CommandMatcher.getServerCommand(
                        GameCommand.COMMAND_MAKE_MOVE, new String[]{MoveConverter.toJSON(point)})
                        .execute(Client.getInstance().getConnection());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            });
        });

        this.dispatcher.register(GameResponse.TYPE_STONE_STATUS_UPDATE, (resp, state) -> {
            Platform.runLater(() -> {
                if (resp.getData() instanceof GameResponse.StoneStatusUpdate) {
                    GameResponse.StoneStatusUpdate data = (GameResponse.StoneStatusUpdate) resp.getData();
                    
                    String[] parts = data.getPosition().split("-");
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    
                    StoneStatus status;
                    if ("dead".equals(data.getStatus())) status = StoneStatus.DEAD;
                    else if ("alive".equals(data.getStatus())) status = StoneStatus.ALIVE;
                    else status = StoneStatus.UNKNOWN;
                    
                    boolean isMine = false;
                    if (data.getSide().equals(this.myColor == project.go.applogic.Color.BLACK ? "black" : "white")) {
                        isMine = true; // Should not happen if server only notifies opponent, but handling it is safe
                    }
                    
                    board.updateStatus(x, y, status, isMine);
                    board.redraw();
                    
                    statusComponent.setStatusText("Status updated: " + data.getPosition() + " to " + data.getStatus(), Status.StatusType.INFO);
                }
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
                    int scoreBlack = data.getScoreBlack();;
                    int scoreWhite = data.getScoreWhite();

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

                    Text info = new Text("Reason: " + data.getReason() + "\nWinner: " + data.getWinner() +
                        "\nFinal Score - Black: " + scoreBlack + " | White: " + scoreWhite);
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
