package project.go.bot;

import java.util.ArrayList;
import java.util.List;

public class Node<T, S extends StatsLike<S>> {
    protected List<Node<T, S>> children; // Child nodes 
    protected T state; // State associated with this node (the move that led to this state)
    protected S stats; // Statistics associated with this node

    public Node(T state, S stats) {
        this.state = state;
        this.stats = stats;
        this.children = new ArrayList<>();
    }

    public T getState() {
        return state;
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
