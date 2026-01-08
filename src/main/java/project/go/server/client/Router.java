package project.go.server.client;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import project.go.server.client.views.MainMenu;

public class Router {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }
    
    /**
     * Routes to the specified path in the application.
     */
    public static void route(Path path) {
        Parent node;
        primaryStage.setScene(null);
        switch (path) {
            case HOME:
                node = new MainMenu();
                break;        
            default:
                node = new MainMenu();
        }

        Scene scene = new Scene(node);
        primaryStage.setScene(scene);
        primaryStage.show();
    }   
}
