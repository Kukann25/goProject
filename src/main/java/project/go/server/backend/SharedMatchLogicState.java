package project.go.server.backend;

/**
 * Shared state between match logic instances (for both players).
 */
public class SharedMatchLogicState {
    // private volatile Board board;
    private MatchState matchState = new MatchState();
    private String blackMove = null;
    private String whiteMove = null;

    public SharedMatchLogicState() {
        // this.board = new Board();
    }
    
    public MatchState getMatchState() {
        return matchState;
    }

    public void setMove(String move, Match.Color color) {
        if (color == Match.Color.BLACK) {
            this.blackMove = move;
        } else if (color == Match.Color.WHITE) {
            this.whiteMove = move;
        }
    }

    public String popEnemyMove(Match.Color color) {
        String move = null;
        if (color == Match.Color.BLACK) {
            move = this.whiteMove;
            this.whiteMove = null;
        } else if (color == Match.Color.WHITE) {
            move = this.blackMove;
            this.blackMove = null;
        }
        return move;
    }
}
