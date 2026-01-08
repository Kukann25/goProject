package project.go.server.client.components;

import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

/**
 * Status component in form of text, placed in the bottom of the window.
 * Simply shows status messages to the user.
 */
public class Status extends HBox {
    private String statusText;
    private Label statusLabel;


    public enum StatusType {
        OK,
        INFO,
        WARNING,
        ERROR
    }

    private static String getColorForStatusType(StatusType type) {
        switch (type) {
            case OK:
                return "#3cf23cff"; // Green
            case INFO:
                return "#dededeff"; // White
            case WARNING:
                return "#e4e433ff"; // Yellow
            case ERROR:
                return "#c40909ff"; // Red
            default:
                return "#e3e3e3ff"; // Default gray
        }
    }

    public Status() {
        this.statusText = "Disconnected";
        this.setMinHeight(30);
        this.setStyle("-fx-background-color: #333333;");
        this.setSpacing(10);
        this.setPadding(new Insets(5, 0, 5, 0));
        this.getChildren().add(new Label("Status: "));
        statusLabel = new Label(this.statusText);
        statusLabel.setTextFill(Color.web(getColorForStatusType(StatusType.INFO)));
        statusLabel.setAlignment(Pos.CENTER_LEFT);
        this.getChildren().add(statusLabel);
    }

    public void setStatusText(String statusText, StatusType type) {
        this.statusText = statusText;
        this.statusLabel.setText(this.statusText);
        this.statusLabel.setTextFill(Color.web(getColorForStatusType(type)));

    }

    public String getStatusText() {
        return this.statusText;
    }
}
