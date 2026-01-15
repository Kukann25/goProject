package project.go.server.backend;

import project.go.Config;
import project.go.applogic.Board;
import project.go.applogic.Color;
import project.go.applogic.MoveConverter;
import project.go.applogic.MoveHandler;
import project.go.applogic.SingleMove;
import project.go.applogic.StoneStatus;
import project.go.applogic.StoneStatusHolder;
import project.go.server.common.json.GameResponse;
import java.util.Queue;
import java.util.LinkedList;

/**
 * Shared state between match logic instances (for both players).
 */
public class SharedMatchLogicState {
    private Board board = new Board(Config.DEFAULT_BOARD_SIZE);
    private MoveHandler moveHandler;
    private final MatchState matchState;
    private String blackMove = null;
    private String whiteMove = null;
    private boolean blackPassed = false;
    private boolean whitePassed = false;
    private Color resumedBy = null;

    private StoneStatusHolder blackProposal;
    private StoneStatusHolder whiteProposal;
    private Queue<GameResponse.StoneStatusUpdate> blackPendingUpdates = new LinkedList<>();
    private Queue<GameResponse.StoneStatusUpdate> whitePendingUpdates = new LinkedList<>();

    public SharedMatchLogicState(MatchState matchState) {
        this.moveHandler = new MoveHandler(board);
        this.matchState = matchState;
    }
    
    public MatchState getMatchState() {
        return matchState;
    }

    public void makeMove(String move, Color color) throws IllegalArgumentException {
        SingleMove sm = MoveConverter.fromJSON(move);
        // Invalid move (regin)
        if (sm.getX() == -1 && sm.getY() == -1) {
            throw new IllegalArgumentException("Pass move is not supported yet");
        }

        if (!moveHandler.makeMove(sm, color)) {
            throw new IllegalArgumentException("Invalid move: " + move);
        }

        // Reset pass flags on valid move
        this.blackPassed = this.whitePassed = false;

        // Move accepted
        if (color == Color.BLACK) {
            this.blackMove = move;
        } else if (color == Color.WHITE) {
            this.whiteMove = move;
        }
    }

    // Resign the match for the given color
    public void resign(Color color) {
        if (color == Color.BLACK) {
            matchState.setWinner(Color.WHITE);
        } else if (color == Color.WHITE) {
            matchState.setWinner(Color.BLACK);
        }
        matchState.addState(MatchState.FORFEITED);
    }

    public void passTurn(Color color) {
        // Currently, passing does not change the board state
        if (color == Color.BLACK) {
            this.blackMove = "pass";
            this.blackPassed = true;
        } else if (color == Color.WHITE) {
            this.whiteMove = "pass";
            this.whitePassed = true;
        }
        this.moveHandler.pass(color);
    }

    public boolean checkBothPassed() {
        return this.blackPassed && this.whitePassed;
    }

    public void resumeGame(Color requestingPlayer) {
        this.blackPassed = false;
        this.whitePassed = false;
        this.blackProposal = null;
        this.whiteProposal = null;
        this.blackPendingUpdates.clear();
        this.whitePendingUpdates.clear();
        
        moveHandler.resumeGame(requestingPlayer);
        this.resumedBy = requestingPlayer;
    }

    public Color popResumed() {
        Color c = this.resumedBy;
        this.resumedBy = null;
        return c;
    }

    public void initNegotiation() {
        this.blackProposal = new StoneStatusHolder(board);
        this.whiteProposal = new StoneStatusHolder(board);
    }

    public void updateStatus(Color color, int x, int y, StoneStatus status) {
        // If x=-1 and y=-1, treat as "All Alive" request
        if (x == -1 && y == -1) {
            updateAllStatus(color, StoneStatus.ALIVE);
            return;
        }

        String sideStr = (color == Color.BLACK) ? "black" : "white";
        // Update the proposal
        if (color == Color.BLACK) {
            blackProposal.updateStatus(x, y, status);
        } else {
            whiteProposal.updateStatus(x, y, status);
        }
        
        // Notify the opponent
        String statusStr = (status == StoneStatus.ALIVE) ? "alive" : (status == StoneStatus.DEAD) ? "dead" : "unknown";
        GameResponse.StoneStatusUpdate update = new GameResponse.StoneStatusUpdate(
            x + "-" + y, statusStr, sideStr
        );

        if (color == Color.BLACK) {
            whitePendingUpdates.add(update);
        } else {
            blackPendingUpdates.add(update);
        }
    }

    private void updateAllStatus(Color color, StoneStatus status) {
        String sideStr = (color == Color.BLACK) ? "black" : "white";
        String statusStr = (status == StoneStatus.ALIVE) ? "alive" : (status == StoneStatus.DEAD) ? "dead" : "unknown";
        
        StoneStatusHolder proposal = (color == Color.BLACK) ? blackProposal : whiteProposal;
        Queue<GameResponse.StoneStatusUpdate> updates = (color == Color.BLACK) ? whitePendingUpdates : blackPendingUpdates;
        
        project.go.applogic.Color[][] cells = board.returnCurrentState();
        int size = board.getSize();
        
        for(int i=0; i<size; i++) {
            for(int j=0; j<size; j++) {
                if(cells[i][j] != project.go.applogic.Color.NONE) {
                    proposal.updateStatus(i, j, status);
                    updates.add(new GameResponse.StoneStatusUpdate(
                        i + "-" + j, statusStr, sideStr
                    ));
                }
            }
        }
    }


    public boolean checkAgreement() {
        if (blackProposal == null || whiteProposal == null) return false;
        return blackProposal.equals(whiteProposal);
    }

    public GameResponse.StoneStatusUpdate popEnemyStatusUpdate(Color color) {
        if (color == Color.BLACK) {
            return blackPendingUpdates.poll();
        } else {
            return whitePendingUpdates.poll();
        }
    }

    public String popEnemyMove(Color color) {
        String move = null;
        if (color == Color.BLACK) {
            move = this.whiteMove;
            this.whiteMove = null;
        } else if (color == Color.WHITE) {
            move = this.blackMove;
            this.blackMove = null;
        }
        return move;
    }

    public Board getBoard() {
        return board;
    }
}
