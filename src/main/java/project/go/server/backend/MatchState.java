package project.go.server.backend;

public class MatchState {
    private int state;

    // Possible states of a match,
    // Note that once a match is COMPLETED or ABORTED
    // it should be cleaned up by MatchManager
    public static final int ONGOING = 0;
    public static final int COMPLETED = 1;
    public static final int ABORTED = 2;
    public static final int CLOSED_CONNECTION = 4;

    public MatchState() {
        this.state = ONGOING;
    }

    public synchronized int getState() {
        return state;
    }

    public synchronized void setState(int newState) {
        this.state = newState;
    }

    public synchronized void addState(int additionalState) {
        this.state |= additionalState;
    }

    public synchronized boolean isOngoing() {
        return this.state == ONGOING;
    }

    public synchronized boolean isClosed() {
        return (CLOSED_CONNECTION & this.state) == CLOSED_CONNECTION;
    }

    public synchronized boolean isCompleted() {
        return (this.state & COMPLETED) == COMPLETED;
    }

    public synchronized boolean isAborted() {
        return (this.state & ABORTED) == ABORTED;
    }
}
