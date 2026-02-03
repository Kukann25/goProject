package project.go.applogic;

/**
 * Class MoveMetrics contains evaluation data for a potential move
 */
public class MoveMetrics {
    public boolean isLegal;
    public int capturedStones;
    public int selfLiberties;
    public int minOpponentLiberties;

    public MoveMetrics() {
        this.isLegal = false;
        this.capturedStones = 0;
        this.selfLiberties = 0;
        this.minOpponentLiberties = Integer.MAX_VALUE;
    }
}
