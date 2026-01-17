package project.go.bot;

import java.util.List;

import project.go.applogic.Board;
import project.go.applogic.Color;
import project.go.applogic.MoveHandler;
import project.go.applogic.SingleMove;

public class GameState extends Board {
    private MoveHandler moveHandler;
    private List<SingleMove> moveHistory;

    public GameState(int size) {
        super(size);
        moveHandler = new MoveHandler(this);
    }

    public void makeMove(SingleMove move) {
        moveHandler.makeMove(move, this.getCurrentTurn());
        moveHistory.add(move);
    }

    // TODO: Implement undoMove method
    public void undoMove() {
        if (moveHistory.isEmpty()) {
            return;
        }
        SingleMove lastMove = moveHistory.remove(moveHistory.size() - 1);
        // moveHandler.undoMove(lastMove);
    }

    // TODO: Implement getLegalMoves method
    public List<SingleMove> getLegalMoves() {
        return null;
    }

    GameState cloneState() {
        GameState clonedState = new GameState(this.getSize());
        Color[][] board = this.returnCurrentState();
        Color[][] clonedBoard = clonedState.returnCurrentState();

        // Clone board state
        for (int x = 0; x < this.getSize(); x++) {
            for (int y = 0; y < this.getSize(); y++) {
                clonedBoard[x][y] = board[x][y];
            }
        }
        // Clone move history
        clonedState.moveHistory = List.copyOf(this.moveHistory);
        return clonedState;
    }
}
