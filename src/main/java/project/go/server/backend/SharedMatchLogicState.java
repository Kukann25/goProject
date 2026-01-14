package project.go.server.backend;

import project.go.Config;
import project.go.applogic.Board;
import project.go.applogic.Color;
import project.go.applogic.MoveConverter;
import project.go.applogic.MoveHandler;
import project.go.applogic.SingleMove;

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
