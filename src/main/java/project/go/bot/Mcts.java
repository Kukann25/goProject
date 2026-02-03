package project.go.bot;

import project.go.applogic.Board;
import project.go.applogic.SingleMove;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class Mcts {
    public static class Config {
        public int nThreads = 1;
    }

    private Node<SingleMove, UcbStats> root;
    private TreeStats treeStats;
    private Limiter limiter;
    private Listener<SingleMove> listener;
    private Strategy<SingleMove, UcbStats> strategy;
    private Config config = new Config();
    private ThreadPoolExecutor executor;
    private GameState gameState;

    public Mcts(Listener<SingleMove> listener, GameState gameState) {
        this.treeStats = new TreeStats();
        this.limiter = new Limiter();
        this.strategy = new UcbStrategy();
        this.listener = listener;
        this.gameState = gameState;

        root = new Node<>(new SingleMove(-1, -1), new UcbStats());
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(config.nThreads);
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        this.root = new Node<>(new SingleMove(-1, -1), new UcbStats());
    }

    public void start() {
        // Prepare for search
        treeStats.reset();
        limiter.start();

        // Start search threads
        for (int i = 0; i < config.nThreads; i++) {
            executor.execute(new Search<>(root, limiter, strategy, treeStats, listener, i, gameState.cloneState()));
        }
    }

    public void stop() {
        limiter.stop();
        // Wait for all threads to finish
        executor.shutdown();
    }

    public TreeStats getTreeStats() {
        return treeStats;
    }

}
