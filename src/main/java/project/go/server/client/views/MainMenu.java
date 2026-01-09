package project.go.server.client.views;

import java.net.ConnectException;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import project.go.server.client.Client;
import project.go.server.client.Path;
import project.go.server.client.Router;
import project.go.server.client.components.Status;

/**
 * Main menu view, shown at the start of the application has:
 * - 'Connect to server' button
 * - 'Exit' button
 */
public class MainMenu extends GridPane {
    private Status statusComponent;


    public MainMenu() {
        this.setPrefHeight(400);
        this.setPrefWidth(600);

        VBox btnContainer = new VBox();

        // Make sure the container takes the whole width
        btnContainer.setPrefWidth(600);
        btnContainer.setMinHeight(200);

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
                Router.route(Path.GAME);

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
        statusComponent.setAlignment(Pos.BOTTOM_CENTER);
        this.add(statusComponent, 0, 1);
    }
}
