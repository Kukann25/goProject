package project.go.bot;

import java.util.ArrayList;

public class ListenerStats<T> {
    public static class SearchLine<T> {
        private ArrayList<T> moves;
        private double winRate;
        private boolean isTerminal;
        private boolean isDraw;

        public SearchLine() {
            moves = new ArrayList<>();
            winRate = 0.0;
            isTerminal = false;
            isDraw = false;
        }

        public SearchLine(ArrayList<T> moves, double winRate, boolean isTerminal, boolean isDraw) {
            this.moves = moves;
            this.winRate = winRate;
            this.isTerminal = isTerminal;
            this.isDraw = isDraw;
        }

        public ArrayList<T> getMoves() {
            return moves;
        }

        public double getWinRate() {
            return winRate;
        }

        public boolean isTerminal() {
            return isTerminal;
        }

        public boolean isDraw() {
            return isDraw;
        }
    }

    private long timeMs = 0;
    private int depth = 0;
    private int simulations = 0;
    private ArrayList<SearchLine<T>> searchLines = new ArrayList<>();

    public ListenerStats() {
    }

    public ListenerStats(long timeMs, int depth, int simulations, ArrayList<SearchLine<T>> searchLines) {
        this.timeMs = timeMs;
        this.depth = depth;
        this.simulations = simulations;
        this.searchLines = searchLines;
    }

    public long getTimeMs() {
        return timeMs;
    }

    public int getDepth() {
        return depth;
    }

    public int getSimulations() {
        return simulations;
    }

    public ArrayList<SearchLine<T>> getSearchLines() {
        return searchLines;
    }
}
