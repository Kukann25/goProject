package project.go.server.client.views;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import project.go.applogic.Board;
import project.go.applogic.Color;
import project.go.applogic.MoveHandler;
import project.go.applogic.SingleMove;
import project.go.server.client.Client;
import project.go.server.client.Path;
import project.go.server.client.Router;
import project.go.server.client.components.BoardComponent;
import project.go.server.client.components.Status;
import project.go.server.common.json.JsonFmt;
import project.go.server.common.json.MatchHistoryResponse;

public class GameHistory extends GridPane {
    private BoardComponent boardComponent;
    private Button nextButton;
    private Button previousButton;
    private Button backButton;
    private Status statusComponent;
    
    private List<Board> boardHistory;
    private List<MatchHistoryResponse.Move> moves;
    private int currentMoveIndex = 0; // Index of the state currently displayed (0 = start)

    public GameHistory() {
        this.boardHistory = new ArrayList<>();
        this.moves = new ArrayList<>();
        this.statusComponent = new Status();

        // 1. Initialize Board (Initial State)
        Board initialBoard = new Board(project.go.Config.DEFAULT_BOARD_SIZE);
        boardHistory.add(initialBoard.copy());
        this.boardComponent = new BoardComponent(initialBoard);
        
        // 2. Fetch Match Data
        String matchId = Client.getInstance().getClientState().getSelectedMatchId();
        fetchMatchData(matchId);

        // 3. UI Setup
        this.previousButton = new Button("Previous");
        this.nextButton = new Button("Next");
        this.backButton = new Button("Back to List");
        
        setupButtons();
        
        VBox controls = new VBox(10);
        HBox navButtons = new HBox(10, previousButton, nextButton);
        navButtons.setAlignment(Pos.CENTER);
        controls.getChildren().addAll(navButtons, backButton);
        controls.setAlignment(Pos.CENTER);
        controls.setStyle("-fx-padding: 10; -fx-background-color: #f0f0f0;");
        
        this.add(boardComponent, 0, 0);
        this.add(controls, 1, 0);
        this.add(statusComponent, 0, 1);
        GridPane.setColumnSpan(statusComponent, 2);

        // 4. Apply first move if exists (as requested)
        if (!moves.isEmpty()) {
            stepForward();
        }
    }
    
    private void fetchMatchData(String matchId) {
        if (matchId == null) {
            statusComponent.setStatusText("No match selected", Status.StatusType.ERROR);
            return;
        }

        try {
            URL url = new URI("http://localhost:8080/history?matchId=" + matchId).toURL();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            
            int status = con.getResponseCode();
            if (status == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    content.append(line);
                }
                in.close();
                con.disconnect();
                
                MatchHistoryResponse resp = JsonFmt.fromJson(content.toString(), MatchHistoryResponse.class);
                if (resp.getMatches() != null && !resp.getMatches().isEmpty()) {
                    MatchHistoryResponse.Match m = resp.getMatches().get(0);
                    this.moves = m.getMoves();
                    if (this.moves == null) this.moves = new ArrayList<>();
                    statusComponent.setStatusText("Loaded match: " + m.getId(), Status.StatusType.OK);
                }
            } else {
                statusComponent.setStatusText("Failed to load match: " + status, Status.StatusType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusComponent.setStatusText("Error loading match", Status.StatusType.ERROR);
        }
    }
    
    private void stepForward() {
        if (currentMoveIndex < moves.size()) {
            // Get current state (at currentMoveIndex)
            Board currentBoard = boardHistory.get(currentMoveIndex).copy();
            
            // Get move to apply (the one at index currentMoveIndex)
            MatchHistoryResponse.Move move = moves.get(currentMoveIndex);
            
            // Check if we already have the NEXT state in history (replay vs compute)
            // But since we are only stepping, we can just compute and add if not present
            // However requirement was "Use a list of Board for undoing".
            
            if (currentMoveIndex + 1 < boardHistory.size()) {
                 // Already computed
            } else {
                // Compute next state
                MoveHandler handler = new MoveHandler(currentBoard);
                try {
                    Color c = "black".equalsIgnoreCase(move.getColor()) ? Color.BLACK : Color.WHITE;
                    handler.makeMove(new SingleMove(move.getX(), move.getY()), c);
                } catch (Exception e) {
                    System.out.println("Error replaying move " + currentMoveIndex + ": " + e.getMessage());
                }
                boardHistory.add(currentBoard);
            }
            
            currentMoveIndex++;
            updateBoard();
        }
    }
    
    private void stepBackward() {
        if (currentMoveIndex > 0) {
            currentMoveIndex--;
            updateBoard();
        }
    }
    
    private void updateBoard() {
        Board b = boardHistory.get(currentMoveIndex);
        boardComponent.update(b); 
        statusComponent.setStatusText("Move " + currentMoveIndex + " / " + moves.size(), Status.StatusType.INFO);
        updateButtons();
    }
    
    private void updateButtons() {
        previousButton.setDisable(currentMoveIndex <= 0);
        nextButton.setDisable(currentMoveIndex >= moves.size());
    }

    private void setupButtons() {
        nextButton.setOnAction(e -> stepForward());
        previousButton.setOnAction(e -> stepBackward());
        backButton.setOnAction(e -> Router.route(Path.GAME_HISTORY_LIST));
        updateButtons();
    }
}
