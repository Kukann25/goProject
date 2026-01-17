package project.go.bot;

import project.go.applogic.SingleMove;

public class Mcts {
    private Node<SingleMove, UcbStats> root;
    private TreeStats treeStats;
    private Limiter limiter;
    private Strategy<SingleMove, UcbStats> strategy;

    public Mcts() {
        this.treeStats = new TreeStats();
        this.limiter = new Limiter();
        this.strategy = new UcbStrategy();
    }

    
}
