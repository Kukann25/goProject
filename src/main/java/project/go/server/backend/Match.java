package project.go.server.backend;

import project.go.server.common.json.*;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

// Represents a match between two players
public class Match implements Runnable {

    private enum Color {
        BLACK,
        WHITE,
        NONE
    }

    public static class Data extends Client.Data {
        private String matchId;
        private Color side;
        
        public Data(final Socket connection) {
            super(connection);
        }

        public Data(final Client.Data base, Color side, final String matchId) {
            super(base.getConnection());
            this.side = side;
            this.matchId = matchId;
        }

        public Color getSide() { return side; }
        public void setSide(Color side) { this.side = side; }
        public String getMatchId() { return matchId; }
        public void setMatchId(String matchId) { this.matchId = matchId; }
    }


    private static class GameThread extends Thread {
        private Match parent;
        private Data data;
        private PrintWriter out;
        private Scanner in;

        public GameThread(Match parent, Data data, PrintWriter out, Scanner in) {
            this.parent = parent;
            this.data = data;
            this.out = out;
            this.in = in;
        }

        private void log(String msg) {
            Logger.getInstance().log("Match-" + parent.matchId, msg);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    out.println(JsonFmt.toJson(handleInput(data, in)));
                }
            } catch (Exception e) {
                // Only log if not caused by input closure
                if (!(e instanceof NoSuchElementException)) {
                    e.printStackTrace();
                }
                parent.childException = e;
                parent.matchThread.interrupt();
            }
        }

        /**
         * Handles input from a player client.
         * @param client The client data.
         * @param in The input scanner for the client.
         * @return GameResponse indicating the result of the command.
         * @throws Exception Internal error processing the command.
         * @throws NoSuchElementException If the input is closed.
         */
        private GameResponse handleInput(Data client, Scanner in) throws Exception, NoSuchElementException {
            // Should be either move or resign command
            String line = in.nextLine();
            GameCommand<?> 
                command = JsonFmt.fromJson(line, GameCommand.class);
            Object payload = command.getPayload();

            if (payload instanceof GameCommand.PayloadMakeMove) {
                return processMove(client, (GameCommand.PayloadMakeMove)payload);
            }

            log("Unknown command from player " + client.data().getClientId() + ": " + command.getCommand());
            return new GameResponse(GameResponse.STATUS_ERROR, GameResponse.MESSAGE_UNKNOWN_COMMAND);
        }

        /**
         * Processes a move command from a player.
         * @param client
         * @param payload
         * @return GameResponse indicating success or failure of the move.
         */
        private GameResponse processMove(Data client, GameCommand.PayloadMakeMove payload) {
            try {
                log("Player " + client.data().getClientId() + " played move: " + payload.getMove());
                return new GameResponse(GameResponse.STATUS_OK, GameResponse.MESSAGE_MOVE_OK);
            }  catch (IllegalArgumentException e) {
                log("Illegal move from player " + client.data().getClientId() + ": " + e.getMessage());
                return new GameResponse(GameResponse.STATUS_ERROR, GameResponse.MESSAGE_INVALID_MOVE);
            }
        }
    }

    private Thread matchThread;
    private Data black;
    private Data white;
    volatile private Exception childException;
    private final String matchId;
    private int state;
    private GameThread blackThread;
    private GameThread whiteThread;

    // Possible states of a match,
    // Note that once a match is COMPLETED or ABORTED
    // it should be cleaned up by MatchManager
    public static final int ONGOING = 0;
    public static final int COMPLETED = 1;
    public static final int ABORTED = 2;
    public static final int CLOSED_CONNECTION = 4;

    public Match(Client.Data cl1, Client.Data cl2) {
        this.state = ONGOING;
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
        this.state = ONGOING;
        matchThread = Thread.currentThread();

        // Run the match until completion
        try {
            PrintWriter blackOut = new PrintWriter(black.getConnection().getOutputStream(), true);
            Scanner blackIn = new Scanner(black.getConnection().getInputStream());

            PrintWriter whiteOut = new PrintWriter(white.getConnection().getOutputStream(), true);
            Scanner whiteIn = new Scanner(white.getConnection().getInputStream());

            // Notify players of match start
            blackOut.println(JsonFmt.toJson(new PlayerTurn(false)));
            whiteOut.println(JsonFmt.toJson(new PlayerTurn(true)));
            
            // Support async input reading to disallow blocking
            // (Always respond to both players even if it's not their turn)
            blackThread = new GameThread(this, black, blackOut, blackIn);
            whiteThread = new GameThread(this, white, whiteOut, whiteIn);           

            blackThread.start();
            whiteThread.start();

            try {
                blackThread.join();
                whiteThread.join();
            } catch (InterruptedException ie) {
                // Interrupted - likely due to error in child thread
                if (childException != null) {
                    throw childException;
                }
            }

        } catch(NoSuchElementException e) {
            // Thrown by Scanner when input is closed
            log("A player disconnected: " + e.getMessage());
            this.state = ABORTED;
        } catch (Exception e) {
            // Other exceptions - internal errors
            e.printStackTrace();
            this.state = ABORTED;
        } finally {
            if ((this.state & ABORTED) != 0) {
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
            if (client.getConnection().isClosed()) return;

            PrintWriter out = new PrintWriter(client.getConnection().getOutputStream(), true);
            log("Closing connection to player " + client.data().getClientId() + " with status " + status);
            out.println(JsonFmt.toJson(new GameResponse(status, message)));
            client.getConnection().close();
            this.state |= CLOSED_CONNECTION;

            if (blackThread != null && blackThread.isAlive()) {
                blackThread.interrupt();
            }
            if (whiteThread != null && whiteThread.isAlive()) {
                whiteThread.interrupt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized public int getState() {
        return state;
    }

    synchronized public boolean isCompleted() {
        return (state & COMPLETED) == COMPLETED;
    }

    synchronized public boolean isAborted() {
        return (state & ABORTED) == ABORTED;
    }

    synchronized public boolean isClosed() {
        return (state & CLOSED_CONNECTION) == CLOSED_CONNECTION;
    }

    public String getMatchId() {
        return matchId;
    }
}
