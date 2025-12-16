package project.go.server.backend;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;

import project.go.server.common.json.GameResponse;
import project.go.server.common.json.JsonFmt;
import project.go.server.common.json.StatusGameResponse;
import project.go.applogic.Color;

// Represents a match between two players
public class Match implements Runnable {

    public static class Data extends Client.Data {
        private String matchId;
        private Color side;
        
        public Data(final Socket connection) {
            super(connection);
        }

        public Data(final Client.Data base, Color side, final String matchId) {
            super(base.getSocket());
            this.side = side;
            this.matchId = matchId;
        }

        public Color getSide() { return side; }
        public void setSide(Color side) { this.side = side; }
        public String getMatchId() { return matchId; }
        public void setMatchId(String matchId) { this.matchId = matchId; }
    }


    private Data black;
    private Data white;
    private final String matchId;
    private MatchState state;

    public Match(Client.Data cl1, Client.Data cl2) {
        this.state = new MatchState();
        this.matchId = java.util.UUID.randomUUID().toString();
        // this.board = new ExtBoard(Config.DEFAULT_BOARD_SIZE); // Standard 19x19 board

        this.black = new Data(cl1, Color.BLACK, matchId);
        this.white = new Data(cl2, Color.WHITE, matchId);
    }

    private static void log(String msg) {
        Logger.getInstance().log("Match", msg);
    }

    @Override
    public void run() {

        // Run the match until completion
        try {
            SharedMatchLogicState sharedState = new SharedMatchLogicState();

            // Support async input reading to disallow blocking
            // (Always respond to both players even if it's not their turn)
            MatchLogic blackLogic = new MatchLogic(black, Color.BLACK, sharedState);
            MatchLogic whiteLogic = new MatchLogic(white, Color.WHITE, sharedState);

            while (this.state.isOngoing()) {
                blackLogic.handleInput();
                whiteLogic.handleInput();
            }

        } catch(NoSuchElementException e) {
            // Thrown by Scanner when input is closed
            log("A player disconnected: " + e.getMessage());
            this.state.setState(MatchState.ABORTED);
        } catch (Exception e) {
            // Other exceptions - internal errors
            e.printStackTrace();
            this.state.setState(MatchState.ABORTED);
        } finally {
            if (this.state.isAborted()) {
                close(black, GameResponse.STATUS_ERROR, GameResponse.MESSAGE_INTERNAL_ERROR);
                close(white, GameResponse.STATUS_ERROR, GameResponse.MESSAGE_INTERNAL_ERROR);
            } else {
                // TODO: Return final scores
                close(black, GameResponse.STATUS_OK, GameResponse.MESSAGE_MATCH_ENDED);
                close(white, GameResponse.STATUS_OK, GameResponse.MESSAGE_MATCH_ENDED);
            }
        }
    }

    /**
     * Closes a client connection with a final message (if not already closed).
     */
    private void close(Data client, int status, String message) {
        try {
            if (client.getSocket().isClosed()) return;

            PrintWriter out = new PrintWriter(client.getSocket().getOutputStream(), true);
            log("Closing connection to player " + client.data().getClientId() + " with status " + status);
            out.println(JsonFmt.toJson(new StatusGameResponse(status, message)));
            client.getSocket().close();
            state.addState(MatchState.CLOSED_CONNECTION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isClosed() {
        return this.state.isClosed();
    }

    public String getMatchId() {
        return matchId;
    }
}
