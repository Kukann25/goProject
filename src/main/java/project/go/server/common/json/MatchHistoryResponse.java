package project.go.server.common.json;

import java.util.ArrayList;
import java.util.List;

public class MatchHistoryResponse {
    
    public static class Move {
        private String color;
        private int x;
        private int y;
        
        public Move() {}
        public Move(String color, int x, int y) {
            this.color = color;
            this.x = x;
            this.y = y;
        }
        public String getColor() { return color; }
        public void setColor(String c) { this.color = c; }
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
    }

    public static class Match {
        private String id;
        private String playerBlack;
        private String playerWhite;
        private List<Move> moves;

        public Match() {}
        public Match(String id, String pb, String pw, List<Move> moves) {
            this.id = id;
            this.playerBlack = pb;
            this.playerWhite = pw;
            this.moves = moves;
        }
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getPlayerBlack() { return playerBlack; }
        public void setPlayerBlack(String p) { this.playerBlack = p; }
        public String getPlayerWhite() { return playerWhite; }
        public void setPlayerWhite(String p) { this.playerWhite = p; }
        public List<Move> getMoves() { return moves; }
        public void setMoves(List<Move> m) { this.moves = m; }
    }
    
    private List<Match> matches = new ArrayList<>();
    
    public MatchHistoryResponse() {}
    public MatchHistoryResponse(List<Match> matches) {
        this.matches = matches;
    }
    
    public List<Match> getMatches() { return matches; }
    public void setMatches(List<Match> matches) { this.matches = matches; }
    public void addMatch(Match m) { this.matches.add(m); }
}
