package project.go.bot;

import java.util.Random;

import project.go.applogic.SingleMove;

public class Search<T,S extends StatsLike<S>> implements Runnable {
    private static final int MAIN_THREAD_ID = 0;
    private static final int VIRTUAL_LOSS = 1;

    private Node<T, S> root;
    private Limiter limiter;
    private Strategy<T, S> strategy;
    private TreeStats treeStats;
    private Listener<T> listener;
    private int threadId;
    private Random random;
    private GameState gameState;

    public Search(Node<T, S> root,
                  Limiter limiter,
                  Strategy<T, S> strategy,
                  TreeStats treeStats,
                  Listener<T> listener,
                  // Unique for started thread
                  int threadId,
                  GameState gameState // must be a clone per thread
                ) 
    {
        this.root = root;
        this.limiter = limiter;
        this.strategy = strategy;
        this.treeStats = treeStats;
        this.listener = listener;
        this.threadId = threadId;
        this.gameState = gameState;
    }

    @Override
    public void run() {
        this.random = new Random(System.currentTimeMillis() + threadId*1000);

        while(this.limiter.isOk(this.treeStats.getSimulations(), treeStats.getMaxDepth())) {
            // Selection
            Node<T, S> selectedNode = this.select();
            float reward = this.simulate();
            strategy.Backpropagate(gameState, selectedNode, reward);

            // Update statistics
            this.treeStats.incrementSimulations();
            this.treeStats.updateSps((int)this.limiter.getElapsedTimeMillis());
        }

        this.limiter.stop();

        // Notify listener if main thread
        if (this.threadId == MAIN_THREAD_ID) {
            // listener.onStop(treeStats);
        }
    }

    private Node<T, S> select() {
        Node<T, S> currentNode = this.root;
        while (!currentNode.children.isEmpty()) {
            currentNode = strategy.Select(currentNode);
        }
        return currentNode;
    }

    private float simulate() {
        return 0.0f;
    }
}
