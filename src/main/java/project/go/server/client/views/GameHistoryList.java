package project.go.server.client.views;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.RouteMatcher.Route;

import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import project.go.server.client.Client;
import project.go.server.client.Path;
import project.go.server.client.Router;
import project.go.server.common.json.MatchHistoryResponse;
import project.go.server.common.json.JsonFmt;

public class GameHistoryList extends VBox {

    private Button backButton;

    public GameHistoryList(Stage primaryStage) {
        primaryStage.setTitle("Game History");
        List<MatchHistoryResponse.Match> matches = fetchMatches();
        ListView<String> listView = new ListView<>();
        
        for (MatchHistoryResponse.Match match : matches) {
            int moves = 0;
            if(match.getMoves() != null){
                moves = match.getMoves().size();
            }
            String label = "Match: " + match.getId() + 
                           " (" + moves + " moves) - vs " + match.getPlayerWhite();
            listView.getItems().add(label);
        }

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            int index = listView.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                String selected = matches.get(index).getId();
                System.out.println("Selected Match ID: " + selected);
                searchGameByID(selected);
                Router.route(Path.GAME_HISTORY);
            }
        });

        this.setPrefWidth(600);
        this.setPrefHeight(400);

        backButton = new Button("Go Back");
        backButton.setOnAction(e -> {
            Router.route(Path.HOME);
        });
        // Center the button and list view
        this.setAlignment(javafx.geometry.Pos.CENTER);

        this.getChildren().add(backButton);
        this.getChildren().add(listView);
    }

    private List<MatchHistoryResponse.Match> fetchMatches() {
        try {
            URL url = new java.net.URI("http://localhost:8080/histories").toURL();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            int status = con.getResponseCode();
            if (status == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                con.disconnect();
                
                MatchHistoryResponse resp = JsonFmt.fromJson(content.toString(), MatchHistoryResponse.class);
                return resp.getMatches();
            } else {
                System.out.println("History request failed code: " + status);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private void searchGameByID(String id) {
        Client.getInstance().getClientState().setSelectedMatchId(id);
    }
}
