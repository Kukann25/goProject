package project.go.bot;

public interface Listener<T> {
    void onDepth(ListenerStats<T> stats);
    void onStop(ListenerStats<T> stats);
}
