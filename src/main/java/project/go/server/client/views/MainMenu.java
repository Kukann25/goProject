package project.go.server.client.views;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import project.go.dbinterface.DBMatch;
import project.go.server.client.Client;
import project.go.server.client.ClientListener;
import project.go.server.client.ClientListenerThread;
import project.go.server.client.Path;
import project.go.server.client.Router;
import project.go.server.client.components.Status;
import project.go.server.client.handlers.ResponseDispatcher;
import project.go.server.common.json.GameModeRequest;
import project.go.server.common.json.GameResponse;
import project.go.server.common.json.JsonFmt;
import javafx.scene.control.ListView;

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

        Button playAgainstAI = new Button("Play Against Bot");
        playAgainstAI.setOnAction(e -> {
            connectAndSend(GameModeRequest.MODE_BOT);
        });
        btnContainer.getChildren().add(playAgainstAI);

        Button connectButton = new Button("Play against Player");
        connectButton.setOnAction(e -> {
            connectAndSend(GameModeRequest.MODE_PVP);
        });
        btnContainer.getChildren().add(connectButton);

        Button gamesHistoryButton = new Button("Game History");
        gamesHistoryButton.setOnAction(e -> {
            openGameHistory();
        });
        btnContainer.getChildren().add(gamesHistoryButton);
        
        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> {
            System.exit(0);
        });
        btnContainer.getChildren().add(exitButton);
        this.add(btnContainer, 0, 0);

        statusComponent = new Status();
        this.add(statusComponent, 0, 1);
    }

    //TODO: WYSZUKANIE MECZU W BAZIE PO ID
    private DBMatch searchGameByID(String id){
        System.out.println("Wybieranie meczu..." + id);
        return null;
        
    }

    private void openGameHistory(){
        Stage stage = new Stage();
        stage.setTitle("Historia Gier");
        DBMatch match1 = new DBMatch();
        List<DBMatch> matches = new ArrayList<>(); //TODO: DODAĆ FUNKCJĘ WRZUCAJĄCĄ MECZE PRZEZ JSON
        matches.add(match1);
        ListView<String> listView = new ListView<>();
        int moves=0;


        for (DBMatch match : matches) {
            if(match.getMoves()!=null){
                moves=match.getMoves().size();
            }
            String label = "Mecz: " + match.getId() + 
                           " (" + moves + " ruchów)";
            listView.getItems().add(label);
        }

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            int index = listView.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                String selected = matches.get(index).getId();
                System.out.println("Wybrano mecz ID: " + selected);
                searchGameByID(selected);
                javafx.stage.Stage stage1 = (javafx.stage.Stage) listView.getScene().getWindow();
                stage1.close();
            }
        });

        VBox layout = new VBox(listView);
        Scene scene = new Scene(layout, 300, 400);
        
        stage.setScene(scene);
        stage.show();
    }
    
    private void connectAndSend(String mode) {
        try {
                if (Client.getInstance().getConnection().isConnected()) {
                    System.out.println("Already connected to server");
                    this.statusComponent.setStatusText("Already connected", Status.StatusType.INFO);
                    return;
                }
                Client.getInstance().getConnection().connect();
                ClientListenerThread.init(
                    Client.getInstance().getClientState(),
                    Client.getInstance().getConnection()
                );
                
                
                ClientListener listener = ClientListenerThread.getInstance().getListener();
                ResponseDispatcher dispatcher = new ResponseDispatcher();
                
                // Add common handler for GameStart or PlayerTurn
                // Note: Logic for waiting for PlayerTurn is what triggers the routing.
                
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
                
                // Send the Mode Request
                // Note: Connection.java send() is synchronized
                // We assume connection is fresh and we just received something (handled by listener thread)
                // or we can just send it now.
                
                GameModeRequest req = new GameModeRequest(mode, null); // id sent by server anyway? Or we just send mode
                // Actually server parses JSON. 
                Client.getInstance().getConnection().send(JsonFmt.toJson(req));
                
                this.statusComponent.setStatusText("Connected, waiting for " + mode + " match...", Status.StatusType.OK);
            } catch (ConnectException ce) {
                System.out.println("Failed to connect to server: " + ce.getMessage());
                this.statusComponent.setStatusText("Connection failed", Status.StatusType.ERROR);
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
                this.statusComponent.setStatusText("Connection error", Status.StatusType.ERROR);
                return;
            }
    }


    void setStatusLater(String text, Status.StatusType type) {
        javafx.application.Platform.runLater(() -> {
            statusComponent.setStatusText(text, type);
        });
    }
}
