package project.go.server.backend;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;

import project.go.server.common.json.GameResponse;
import project.go.server.common.json.JsonFmt;
import project.go.applogic.Color;

// Represents a match between two players
public class Match implements Runnable {

    public static class ClientData extends ConnectedClient.Data {
        private String matchId;
        private Color side;
        
        public ClientData(final Socket connection) {
            super(connection);
        }

        public ClientData(final ConnectedClient.Data base, Color side, final String matchId) {
            super(base.getSocket());
            this.side = side;
            this.matchId = matchId;
        }

        public Color getSide() { return side; }
        public void setSide(Color side) { this.side = side; }
        public String getMatchId() { return matchId; }
        public void setMatchId(String matchId) { this.matchId = matchId; }
    }

    private ClientData black;
    private ClientData white;
    private final String matchId;
    private MatchState state;

    public Match(ConnectedClient.Data cl1, ConnectedClient.Data cl2) {
        this.state = new MatchState();
        this.matchId = java.util.UUID.randomUUID().toString();
        // this.board = new ExtBoard(Config.DEFAULT_BOARD_SIZE); // Standard 19x19 board

        this.black = new ClientData(cl1, Color.BLACK, matchId);
        this.white = new ClientData(cl2, Color.WHITE, matchId);
    }

    private static void log(String msg) {
        Logger.getInstance().log("Match", msg);
    }

    @Override
    public void run() {

        // Run the match until completion
        try {
            SharedMatchLogicState sharedState = new SharedMatchLogicState(this.state);

            // Support async input reading to disallow blocking
            // (Always respond to both players even if it's not their turn)
            MatchLogic blackLogic = new MatchLogic(black, sharedState);
            MatchLogic whiteLogic = new MatchLogic(white, sharedState);

            while (this.state.isOngoing()) {
                blackLogic.handleInput();
                whiteLogic.handleInput();
            }

        } catch(NoSuchElementException e) {
            // Thrown by Scanner when input is closed
            log("A player disconnected: " + e.getMessage());
            this.state.setState(MatchState.ABORTED | MatchState.CLOSED_CONNECTION);
        } catch (Exception e) {
            // Other exceptions - internal errors
            e.printStackTrace();
            this.state.setState(MatchState.ABORTED);
        } finally {
            if (this.state.isAborted()) {
                close(black, new GameResponse<GameResponse.MatchEnd>(
                    GameResponse.STATUS_FATAL,
                    GameResponse.TYPE_MATCH_END,
                    "Match aborted due to error or disconnection",
                    new GameResponse.MatchEnd(
                        GameResponse.MatchEnd.REASON_ERROR,
                        GameResponse.MatchEnd.WINNER_NONE)));

                close(white, new GameResponse<GameResponse.MatchEnd>(
                    GameResponse.STATUS_FATAL,
                    GameResponse.TYPE_MATCH_END,
                    "Match aborted due to error or disconnection",
                    new GameResponse.MatchEnd(
                        GameResponse.MatchEnd.REASON_ERROR,
                        GameResponse.MatchEnd.WINNER_NONE)));
            } else {
                notifyOnGameEnd();
            }
        }
    }

    /**
     * Closes a client connection with a final message (if not already closed).
     */
    private void close(ClientData client, Object response) {
        try {
            if (client.getSocket().isClosed()) return;

            PrintWriter out = new PrintWriter(client.getSocket().getOutputStream(), true);
            log("Closing connection to player " + client.data().getClientId());
            out.println(JsonFmt.toJson(response));
            client.getSocket().close();
            state.addState(MatchState.CLOSED_CONNECTION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyOnGameEnd() {
        GameResponse.MatchEnd matchEndResponse;
        Color winner = state.getWinner();
        String message;
        String reason;
        
        if (state.isForfeited()) {
            reason = "forfeit";
        } else {
            reason = "normal";
        }

        if (winner == Color.BLACK) {
            message = "Black player wins!";
            matchEndResponse = new GameResponse.MatchEnd(
                reason, GameResponse.MatchEnd.WINNER_BLACK);
        } else if (winner == Color.WHITE) {
            message = "White player wins!";
            matchEndResponse = new GameResponse.MatchEnd(
                reason, GameResponse.MatchEnd.WINNER_WHITE);
        } else {
            message = "The game ended in a draw.";
            matchEndResponse = new GameResponse.MatchEnd(
                reason, GameResponse.MatchEnd.WINNER_NONE);
        }

        close(black, new GameResponse<GameResponse.MatchEnd>(
            GameResponse.STATUS_OK,
            GameResponse.TYPE_MATCH_END,
            message,
            matchEndResponse));
        
        close(white, new GameResponse<GameResponse.MatchEnd>(
            GameResponse.STATUS_OK,
            GameResponse.TYPE_MATCH_END,
            message,
            matchEndResponse));
    }

    public boolean isClosed() {
        return this.state.isClosed();
    }

    public String getMatchId() {
        return matchId;
    }
}
