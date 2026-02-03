package project.go.server.backend;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;

import project.go.server.common.json.GameResponse;
import project.go.server.common.json.JsonFmt;
import project.go.applogic.Color;
import project.go.applogic.MoveHandler;
import project.go.dbinterface.DBMatch;
import project.go.dbinterface.MatchRepository;

// Represents a match between two players
public class Match implements Runnable {

    public static class ClientData extends ConnectedClient.Data {
        private String matchId;
        private Color side;
        
        public ClientData(final Socket connection) {
            super(connection);
        }

        public ClientData(final ConnectedClient.Data base, Color side, final String matchId) {
            super(base.getSocket(), base.getClientId());
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
    private MatchParticipantStrategy blackStrategy;
    private MatchParticipantStrategy whiteStrategy;

    private final String matchId;
    private MatchState state;
    private SharedMatchLogicState sharedState;
    private MatchRepository matchDBRepository;

    public Match(ConnectedClient.Data cl1, ConnectedClient.Data cl2, MatchRepository matchDBRepository) {
        this.state = new MatchState();
        this.matchId = java.util.UUID.randomUUID().toString();
        this.black = new ClientData(cl1, Color.BLACK, matchId);
        this.white = new ClientData(cl2, Color.WHITE, matchId);

        this.matchDBRepository = matchDBRepository;
    }
    
    // PvBot Match Constructor
    public Match(ConnectedClient.Data player, boolean playerIsBlack, MatchRepository matchDBRepository) {
        this.state = new MatchState();
        this.matchId = java.util.UUID.randomUUID().toString();
        this.matchDBRepository = matchDBRepository;
        
        if (playerIsBlack) {
            this.black = new ClientData(player, Color.BLACK, matchId);
            this.white = createBotData(Color.WHITE, matchId);
        } else {
            this.black = createBotData(Color.BLACK, matchId);
            this.white = new ClientData(player, Color.WHITE, matchId);
        }
    }
    
    private ClientData createBotData(Color color, String matchId) {
         // Create dummy socket/data for bot, or handle differently.
         // Since BotStrategy doesn't use the socket, we can pass null or a dummy.
         // For safety with existing code that might check getSocket(), we might need a mock.
         // However, Strategy pattern avoids this issue if implemented correctly.
         return new ClientData((Socket)null) {
             @Override
             public Color getSide() { return color; }
             @Override
             public String getMatchId() { return matchId; }
             @Override
             public String getClientId() { return "Bot"; }
         };
    }

    private static void log(String msg) {
        Logger.getInstance().log("Match", msg);
    }

    @Override
    public void run() {

        // Run the match until completion
        try {
            sharedState = new SharedMatchLogicState(this.state);
            
            // Initialize Strategies
            if (black.getClientId().equals("Bot")) {
                blackStrategy = new BotParticipantStrategy(Color.BLACK, sharedState);
            } else {
                blackStrategy = new HumanParticipantStrategy(black, sharedState);
            }

            if (white.getClientId().equals("Bot")) {
                whiteStrategy = new BotParticipantStrategy(Color.WHITE, sharedState);
            } else {
                whiteStrategy = new HumanParticipantStrategy(white, sharedState);
            }
            
            // Send Game Start info to humans
            notifyGameStart(black, white.getClientId().equals("Bot"));
            notifyGameStart(white, black.getClientId().equals("Bot"));

            boolean notifiedPass = false;

            while (this.state.isOngoing()) {
                blackStrategy.handleTurn(this);
                whiteStrategy.handleTurn(this);

                if (sharedState.checkBothPassed() && !notifiedPass) {
                    notifiedPass = true;
                    // Only start negotiation if NO bots are involved, or handle specially
                    boolean blackCanNegotiate = blackStrategy.supportsNegotiation();
                    boolean whiteCanNegotiate = whiteStrategy.supportsNegotiation();
                    
                    if (blackCanNegotiate && whiteCanNegotiate) {
                         sharedState.initNegotiation();
                         log("Both players passed. Starting match end negotiation.");
                         beginNegotiationOnPass(black);
                         beginNegotiationOnPass(white);
                    } else {
                        // PvP Bot or Bot vs Bot (unlikely) -> End game directly using server scoring
                        log("Both passed. Bot involved, skipping negotiation and ending game.");
                        state.setWinner(null); // End cleanly
                        notifyOnGameEnd(); 
                        return; // Exit loop
                    }
                } else if (!sharedState.checkBothPassed() && notifiedPass) {
                    // If negotiation was ongoing but now one player made a move, resume the game
                    notifiedPass = false;
                    log("Negotiation cancelled. Resuming game.");
                }
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
                DBMatch dbMatch = new DBMatch();
                dbMatch.setPlayerBlack(black.getClientId());
                dbMatch.setPlayerWhite(white.getClientId());
                dbMatch.setMoves(sharedState.getDBMoves());
                matchDBRepository.save(dbMatch);
                notifyOnGameEnd();
            }
        }
    }

    private void notifyGameStart(ClientData client, boolean opponentIsBot) {
        if (client.getSocket() == null) return; // Is a bot or disconnected
        try {
            PrintWriter out = new PrintWriter(client.getSocket().getOutputStream(), true);
            String matchType = opponentIsBot ? GameResponse.MATCH_TYPE_PVBOT : GameResponse.MATCH_TYPE_PVP;
            String opponentName = opponentIsBot ? "Bot" : "Player";
            
            out.println(JsonFmt.toJson(
                new GameResponse<GameResponse.GameStart>(
                    GameResponse.STATUS_OK,
                    GameResponse.TYPE_GAME_START,
                    "Match Started",
                    new GameResponse.GameStart(matchType, opponentName)
                )
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void beginNegotiationOnPass(ClientData client) throws Exception {
        // Notify both players that both have passed
        PrintWriter out = new PrintWriter(client.getSocket().getOutputStream(), true);

        out.println(JsonFmt.toJson(
            new GameResponse<Object>(
                GameResponse.STATUS_OK,
                GameResponse.TYPE_PASS_MOVE,
                "Both players have passed. Match end negotiation started.",
                null)
        ));
    }

    /**
     * Closes a client connection with a final message (if not already closed).
     */
    private void close(ClientData client, Object response) {
        if (client.getSocket() == null) return; // Bot has no socket

        try {
            if (client.getSocket().isClosed()) return;

            PrintWriter out = new PrintWriter(client.getSocket().getOutputStream(), true);
            log("Closing connection to player " + client.data().getClientId());
            out.println(JsonFmt.toJson(response));
            client.getSocket().close();
            state.addState(MatchState.CLOSED_CONNECTION); // Only set this if a real player leaves? 
            // Actually closed connection state usually means game over anyway.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyOnGameEnd() {
        GameResponse.MatchEnd matchEndResponse;
        Color winner = state.getWinner();
        String message;
        String reason;

        MoveHandler mh = sharedState.getMoveHandler();

        if (sharedState.getWhiteProposal() != null) {
            mh.removeStones(sharedState.getWhiteProposal()); // Both black and white agreed on these
        }

        mh.resolvePoints();
        int scoreBlack = mh.returnPoints(Color.BLACK);
        int scoreWhite = mh.returnPoints(Color.WHITE);

        if (state.isForfeited()) {
            reason = "forfeit";
        } else {
            reason = "normal";

            // Determine winner by score if no forfeit
            if (scoreBlack > scoreWhite) {
                winner = Color.BLACK;
            } else if (scoreWhite > scoreBlack) {
                winner = Color.WHITE;
            } else {
                winner = null; // draw
            }
        }

        if (winner == Color.BLACK) {
            message = "Black player wins!";
            matchEndResponse = new GameResponse.MatchEnd(
                reason, GameResponse.MatchEnd.WINNER_BLACK, scoreBlack, scoreWhite);
        } else if (winner == Color.WHITE) {
            message = "White player wins!";
            matchEndResponse = new GameResponse.MatchEnd(
                reason, GameResponse.MatchEnd.WINNER_WHITE, scoreBlack, scoreWhite);
        } else {
            message = "The game ended in a draw.";
            matchEndResponse = new GameResponse.MatchEnd(
                reason, GameResponse.MatchEnd.WINNER_NONE, scoreBlack, scoreWhite);
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
