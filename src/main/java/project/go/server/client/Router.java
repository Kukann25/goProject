package project.go.server.client;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import project.go.server.client.views.Game;
import project.go.server.client.views.GameHistory;
import project.go.server.client.views.GameHistoryList;
import project.go.server.client.views.MainMenu;

public class Router {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }
    
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Routes to the specified path in the application.
     */
    public static void route(Path path, Object... params) {
        Parent node;
        switch (path) {
            case HOME:
                node = new MainMenu();
                break;
            case GAME:
                node = new Game(primaryStage, (Game.Props) params[0]);
                break;
            case GAME_HISTORY_LIST:
                node = new GameHistoryList(primaryStage);
                break;
            case GAME_HISTORY:
                node = new GameHistory();
                break;
            default:
                node = new MainMenu();
        }

        Scene scene = new Scene(node);
        primaryStage.setScene(scene);
        primaryStage.show();
    }   
}
