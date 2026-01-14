package project.go.server.backend;

import project.go.applogic.Color;

public class MatchState {
    private int state;

    // Possible states of a match,
    // Note that once a match is COMPLETED or ABORTED
    // it should be cleaned up by MatchManager
    public static final int ONGOING = 0;
    public static final int COMPLETED = 1;
    public static final int ABORTED = 2;

    // Additional states
    public static final int FORFEITED = 4;
    public static final int CLOSED_CONNECTION = 8;
    public static final int BLACK_WON = 16;
    public static final int WHITE_WON = 32;

    public MatchState() {
        this.state = ONGOING;
    }

    public synchronized int getState() {
        return state;
    }

    public synchronized void setState(int newState) {
        this.state = newState;
    }

    public void addState(int additionalState) {
        this.state |= additionalState;
    }

    public synchronized boolean isOngoing() {
        return this.state == ONGOING;
    }

    // Means one of the players aborted the match (but didn't forfeit)
    public synchronized boolean isAborted() {
        return (this.state & ABORTED) == ABORTED;
    }

    // Means one of the players closed the connection
    public synchronized boolean isClosed() {
        return (CLOSED_CONNECTION & this.state) == CLOSED_CONNECTION;
    }

    // Means the match reached a normal conclusion (e.g. someone won)
    public synchronized boolean isCompleted() {
        return (this.state & COMPLETED) == COMPLETED;
    }

    public synchronized void forfeit(Color color) {
        if (color == Color.BLACK) {
            this.addState(WHITE_WON);
        } else if (color == Color.WHITE) {
            this.addState(BLACK_WON);
        }
        this.addState(COMPLETED);
        this.addState(FORFEITED);
    }

    // Means one of the players forfeited the match
    // If this is true, then isCompleted() should also be true as well as
    // one of BLACK_WON or WHITE_WON flags
    public synchronized boolean isForfeited() {
        return (this.state & FORFEITED) == FORFEITED;
    }

    public synchronized void setWinner(Color color) {
        if (color == Color.BLACK) {
            this.addState(BLACK_WON);
        } else if (color == Color.WHITE) {
            this.addState(WHITE_WON);
        }
        this.addState(COMPLETED);
    }

    public synchronized Color getWinner() {
        if ((this.state & BLACK_WON) == BLACK_WON) {
            return Color.BLACK;
        } else if ((this.state & WHITE_WON) == WHITE_WON) {
            return Color.WHITE;
        } else {
            return null;
        }
    }

    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MatchState: ");
        if (isOngoing()) {
            sb.append("ONGOING ");
        }
        if (isCompleted()) {
            sb.append("COMPLETED ");
        }
        if (isAborted()) {
            sb.append("ABORTED ");
        }

        if (isForfeited()) {
            sb.append("FORFEITED ");
        }
        if (isClosed()) {
            sb.append("CLOSED_CONNECTION ");
        }
        if (getWinner() != null) {
            sb.append("WINNER: " + getWinner().toString() + " ");
        }
        return sb.toString();
    }
}
