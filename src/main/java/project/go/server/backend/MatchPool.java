package project.go.server.backend;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import project.go.server.common.json.GameModeRequest;
import project.go.Config;
import project.go.dbinterface.MatchRepository;

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
    private MatchRepository matchDBRepository;
    
    public MatchPool(ClientPool clientPool, MatchRepository matchDBRepository) {
        this.clientPool = clientPool;
        this.matches = new HashMap<>();
        this.matchPool = Executors.newFixedThreadPool((Config.MAX_CLIENTS / 2) + 1);
        this.matchDBRepository=matchDBRepository;
    }

    /**
     * Runs one iteration of the matchmaker to pair awaiting clients into matches.
     */
    private void runMatchMaker() {
        if (!isRunning) {
            return;
        }
        try {
            // Check awaiting clients
            Vector<ConnectedClient> awaitingClients = clientPool.getAwaitingClients();
            long currentTime = System.currentTimeMillis();
            
            // Filter clients by mode
            Vector<ConnectedClient> pvpClients = new Vector<>();
            Vector<ConnectedClient> botClients = new Vector<>();

            for (ConnectedClient c : awaitingClients) {
                String mode = c.getGameMode();
                if (GameModeRequest.MODE_BOT.equals(mode)) {
                    botClients.add(c);
                } else {
                    pvpClients.add(c);
                }
            }

            // Handle Bot Requests immediately
            for (ConnectedClient client : botClients) {
                 client.join(); // Mark as IN_MATCH
                 Match match = new Match(client.getClientData(), true, matchDBRepository);
                 matches.put(match.getMatchId(), match);
                 matchPool.execute(new MatchRunWrapper(match, matches));
                 Logger.getInstance().log("MatchPool", "Started Player vs Bot match.");
            }

            // Handle PvP Requests
            while (pvpClients.size() >= 2) {
                ConnectedClient cl1 = pvpClients.remove(0);
                ConnectedClient cl2 = pvpClients.remove(0);
                cl1.join();
                cl2.join();
                Match match = new Match(cl1.getClientData(), cl2.getClientData(), matchDBRepository);
                matches.put(match.getMatchId(), match);
                matchPool.execute(new MatchRunWrapper(match, matches));
            }
            
            // PvP fallback to bot (if wait > 10s)
            // if (!pvpClients.isEmpty()) {
            //     ConnectedClient client = pvpClients.get(0);
            //     if (currentTime - client.getJoinTime() > 10000) { // 10 seconds
            //         // pvpClients.remove(0); // Not needed as we reconstruct list next time
            //         client.join();
                    
            //         boolean isBlack = Math.random() < 0.5;
            //         Match match = new Match(client.getClientData(), isBlack);
            //         matches.put(match.getMatchId(), match);
            //         matchPool.execute(new MatchRunWrapper(match, matches));
            //         Logger.getInstance().log("MatchPool", "Paired PvP client with Bot after timeout.");
            //     }
            // }
            
            Thread.sleep(100); // Avoid busy waiting
            Thread.yield(); // Allow other threads to run
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
