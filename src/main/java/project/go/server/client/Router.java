package project.go.server.client;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import project.go.server.client.views.Game;
import project.go.server.client.views.MainMenu;

public class Router {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
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
                node = new Game((Game.Props) params[0]);
                break;      
            default:
                node = new MainMenu();
        }

        Scene scene = new Scene(node);
        primaryStage.setScene(scene);
        primaryStage.show();
    }   
}
