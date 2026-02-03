package project.go.bot;

import project.go.applogic.Color;
import project.go.applogic.MoveHandler;
import project.go.applogic.MoveMetrics;
import project.go.applogic.SingleMove;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Simple engine to play Go.
 * Uses a heuristic strategy:
 * 1. Maximize captures.
 * 2. Put enemies in Atari (1 liberty) or choke them.
 * 3. Expand own chains (maximize liberties).
 */
public class Engine {
    
    private Random random = new Random();

    public boolean shouldPass(MoveHandler moveHandler, Color side) {
        return random.nextDouble() < 0.05; // 5% chance to pass randomly
    }

    /**
     * Returns the best move for the given side using heuristics.
     * 
     * @param moveHandler The handler for the current game state
     * @param side The side to move
     * @return The selected SingleMove, or null if player should pass
     */
    public SingleMove returnBestMove(MoveHandler moveHandler, Color side) {
        List<SingleMove> legalMoves = moveHandler.generateLegalMoves(side);
        
        if (legalMoves.isEmpty()) {
            return null; 
        }

        // We will store moves with their score
        class ScoredMove implements Comparable<ScoredMove> {
            SingleMove move;
            double score;

            ScoredMove(SingleMove move, double score) {
                this.move = move;
                this.score = score;
            }

            @Override
            public int compareTo(ScoredMove other) {
                return Double.compare(other.score, this.score); // Descending
            }
        }

        List<ScoredMove> scoredMoves = new ArrayList<>();

        for (SingleMove move : legalMoves) {
            MoveMetrics metrics = moveHandler.evaluateMove(move.getX(), move.getY(), side);
            
            if (!metrics.isLegal) continue; // Should be legal from generateLegalMoves, but safe check

            double score = 0;
            
            score += metrics.capturedStones * 100.0;
            if (metrics.minOpponentLiberties < 1000) { // If there were any neighbors
                 if (metrics.minOpponentLiberties == 1) {
                     score += 50.0; // Atari
                 } else {
                     // small bonus for reducing liberties, more points for fewer liberties
                     score += (10.0 / metrics.minOpponentLiberties);
                 }
            }

            // Priority 3: Expand own chains (increase safety).
            // More liberties = better.
            score += metrics.selfLiberties * 1.0;
            
            // Small randomness to avoid deterministic loops and make games varied
            score += random.nextDouble();

            scoredMoves.add(new ScoredMove(move, score));
        }
        
        if (scoredMoves.isEmpty()) return null;

        Collections.sort(scoredMoves);
        
        // Return the best one
        return scoredMoves.get(0).move;
    }
}
