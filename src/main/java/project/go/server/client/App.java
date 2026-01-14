package project.go.server.client;

import javafx.application.Application;

public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(javafx.stage.Stage primaryStage) throws Exception {
        Router.setPrimaryStage(primaryStage);
        Router.route(Path.HOME);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        
        SyncPrinter.info("Shutting down client...");
        if (ClientListenerThread.getInstance() != null) {
            ClientListenerThread.getInstance().kill();
        }
    }
}
