package project.go.bot;

import project.go.applogic.SingleMove;

public class UcbStrategy implements Strategy<SingleMove, UcbStats> {
    @Override
    public Node<SingleMove, UcbStats> Select(Node<SingleMove, UcbStats> node) {
        // Implementation of UCB selection logic
        return null; // Placeholder
    }

    @Override
    public void Backpropagate(GameState state, Node<SingleMove, UcbStats> node, float reward) {
        // Implementation of backpropagation logic
    }
    
}
