package project.go.server.backend;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import project.go.Config;

public class MatchPool {

    /**
     * Wrapper to run a match and remove it from the pool upon completion.
     */
    public static class MatchRunWrapper implements Runnable {
        private final Match match;
        private final HashMap<String, Match> matches;

        public MatchRunWrapper(Match match, HashMap<String, Match> matches) {
            this.match = match;
            this.matches = matches;
        }

        @Override
        public void run() {
            match.run();
            
            // After match ends, remove it from the pool
            synchronized (matches) {
                if (match.isClosed()) {
                    matches.remove(match.getMatchId());
                }
            }
        }
    }

    private final HashMap<String, Match> matches;
    private final ExecutorService matchPool;
    private final ClientPool clientPool;
    private volatile boolean isRunning = true;
    private Thread matchMakerThread;
    
    public MatchPool(ClientPool clientPool) {
        this.clientPool = clientPool;
        this.matches = new HashMap<>();
        this.matchPool = Executors.newFixedThreadPool((Config.MAX_CLIENTS / 2) + 1);
    }

    /**
     * Runs one iteration of the matchmaker to pair awaiting clients into matches.
     */
    private void runMatchMaker() {
        if (!isRunning) {
            return;
        }
        try {
            Vector<Client> awaitingClients = clientPool.getAwaitingClients();
            while (awaitingClients.size() >= 2) {
                Client cl1 = awaitingClients.remove(0);
                Client cl2 = awaitingClients.remove(0);
                cl1.join();
                cl2.join();
                Match match = new Match(cl1.getClientData(), cl2.getClientData());
                matches.put(match.getMatchId(), match);
                matchPool.execute(new MatchRunWrapper(match, matches));
            }
            Thread.sleep(100); // Avoid busy waiting
        } catch (InterruptedException e) {
            Logger.getInstance().error("MatchPool", "Matchmaker interrupted: " + e.getMessage());
        }
    }

    /**
     * Starts the match pool and continuously runs the matchmaker.
     */
    public void start() {
        matchMakerThread = new Thread(() -> {
            while (isRunning) {
                runMatchMaker();
                Thread.yield();
            }
        });
        matchMakerThread.start();
    }

    /**
     * Shuts down the match pool and stops all matches.
     */
    public void shutdown() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        try {
            matchMakerThread.join();
        } catch (InterruptedException e) {
            Logger.getInstance().error("MatchPool", "Error stopping matchmaker thread: " + e.getMessage());
        }
        matchPool.shutdown();
    }
}
