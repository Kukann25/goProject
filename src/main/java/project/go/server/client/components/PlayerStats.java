package project.go.server.client.components;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

public class PlayerStats extends VBox {
    // Right now simply display the player's color
    private project.go.applogic.Color playerColor;
    private Label playerColorLabel;

    public PlayerStats(project.go.applogic.Color color) {
        this.playerColor = color;
        
        playerColorLabel = new Label("Player Color: " + playerColor);
        playerColorLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        this.getChildren().add(playerColorLabel);
    }
}
