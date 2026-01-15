package project.go.applogic;

/**
 * Color of the stones
 */
public enum Color {
    BLACK,
    WHITE,
    NONE;

    public Color getOpposite() {
        if (this == BLACK) {
            return WHITE;
        } else if (this == WHITE) {
            return BLACK;
        } else {
            return NONE;
        }
    }
}
