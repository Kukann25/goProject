package project.go.bot;

public interface Strategy<T, S extends StatsLike<S>> {
    Node<T, S> Select(Node<T, S> node);
    void Backpropagate(GameState state, Node<T, S> node, float reward);
}
