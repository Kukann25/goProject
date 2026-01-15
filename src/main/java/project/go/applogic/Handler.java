package project.go.applogic;

public class Handler {
    protected Board board;

    protected final int[] DX = {-1, 1, 0, 0};
    protected final int[] DY = {0, 0, -1, 1};

    /**
     * Checks whether move is in bounds of a board
     * @param x
     * @param y
     * @return 
     */
    protected boolean isValid(int x, int y) {
        if (x < 0 || x >= board.getSize() || y < 0 || y >= board.getSize()) {
            return false;
        }
        return true;
    }
}
