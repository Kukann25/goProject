package project.go.bot;

import java.util.ArrayList;
import java.util.List;

public class Node<T, S extends StatsLike<S>> {
    protected List<Node<T, S>> children; // Child nodes 
    protected T move; // move associated with this node (the move that led to this move)
    protected S stats; // Statistics associated with this node

    public Node(T move, S stats) {
        this.move = move;
        this.stats = stats;
        this.children = new ArrayList<>();
    }

    public T getMove() {
        return move;
    }

    public S getStats() {
        return stats;
    }

    public void addChildren(List<Node<T, S>> children) {
        this.children.addAll(children);
    }

    public List<Node<T, S>> getChildren() {
        return children;
    }
}
