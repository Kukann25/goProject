package project.go.server.client.views;

import java.net.ConnectException;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import project.go.server.client.Client;
import project.go.server.client.ClientListener;
import project.go.server.client.Path;
import project.go.server.client.Router;
import project.go.server.client.components.Status;
import project.go.server.client.handlers.ResponseDispatcher;
import project.go.server.common.json.GameCommand;
import project.go.server.common.json.GameResponse;

/**
 * Main menu view, shown at the start of the application has:
 * - 'Connect to server' button
 * - 'Exit' button
 */
public class MainMenu extends GridPane {
    private Status statusComponent;


    public MainMenu() {
        VBox btnContainer = new VBox();

        // Make sure the container takes the whole width
        btnContainer.setPrefWidth(300);
        btnContainer.setPrefHeight(200);

        btnContainer.setAlignment(Pos.CENTER);
        btnContainer.setSpacing(10);

        Button connectButton = new Button("Connect to Server");
        connectButton.setOnAction(e -> {
            try {
                if (Client.getInstance().getConnection().isConnected()) {
                    System.out.println("Already connected to server");
                    this.statusComponent.setStatusText("Already connected", Status.StatusType.INFO);
                    return;
                }
                Client.getInstance().getConnection().connect();
                ClientListener.init(
                    Client.getInstance().getClientState(),
                    Client.getInstance().getConnection()
                );
                ClientListener listener = ClientListener.getInstance();
                ResponseDispatcher dispatcher = new ResponseDispatcher();
                dispatcher.register(GameResponse.TYPE_PLAYER_TURN, (resp, state) -> {
                    if (resp.isError()) {
                        System.out
                            .println("Error receiving player turn: " + resp.getMessage());
                        this.setStatusLater(
                            "Error receiving player turn: " + resp.getMessage(),
                            Status.StatusType.ERROR
                        );
                        return;
                    }

                    if (!(resp.getData() instanceof GameResponse.PlayerTurn)) {
                        System.out
                            .println("Invalid player turn data received from server");
                        this.setStatusLater(
                            "Invalid player turn data received from server",
                            Status.StatusType.ERROR
                        );
                        return;
                    }

                    // Update client state with player turn info
                    GameResponse.PlayerTurn pt = (GameResponse.PlayerTurn) resp.getData();
                    state.setPlayerColor(pt.getColor());

                    // On receiving player turn, route to game view
                    javafx.application.Platform.runLater(() -> {
                        Router.route(Path.GAME, new Game.Props((GameResponse.PlayerTurn)resp.getData()));
                    });
                });
                
                listener.setDispatcher(dispatcher);
                this.statusComponent.setStatusText("Connected to server", Status.StatusType.OK);
            } catch (ConnectException ce) {
                System.out.println("Failed to connect to server: " + ce.getMessage());
                this.statusComponent.setStatusText("Connection failed", Status.StatusType.ERROR);
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
                this.statusComponent.setStatusText("Connection error", Status.StatusType.ERROR);
                return;
            }
        });
        btnContainer.getChildren().add(connectButton);
        
        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> {
            System.exit(0);
        });
        btnContainer.getChildren().add(exitButton);
        this.add(btnContainer, 0, 0);

        statusComponent = new Status();
        this.add(statusComponent, 0, 1);
    }


    void setStatusLater(String text, Status.StatusType type) {
        javafx.application.Platform.runLater(() -> {
            statusComponent.setStatusText(text, type);
        });
    }
}
