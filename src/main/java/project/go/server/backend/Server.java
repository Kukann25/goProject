package project.go.server.backend;


import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import project.go.Config;
import project.go.dbinterface.MatchRepository;


public class Server {

    // Main server socket, the server is listening on
    private ServerSocket serverSocket;

    // Thread pool for handling client connections, that is waiting for 
    // the match to start and send upon connection data
    private ClientPool clientPool;

    // Managers for clients and matches
    private MatchPool matchPool;

    // Server running state
    private boolean isRunning = true;

    // Thread for asynchronous server running
    private Thread serverThread;

    private MatchRepository matchDBRepository;

    /**
     * Initializes the server on the specified port.
     * @throws Exception If the server socket cannot be created (e.g., port is in use).
     */
    public Server(MatchRepository matchDBRepository) throws Exception {
        serverSocket = new ServerSocket(Config.PORT);
        Logger.getInstance().setLogLevel(Logger.LEVEL_ALL);
        this.matchDBRepository=matchDBRepository;
    }

    /**
     * Initializes the server on the specified port with a given log level.
     * @param logLevel The logging level to use.
     * @param port The port number on which the server will listen.
     * @throws Exception If the server socket cannot be created (e.g., port is in use).
     */
    public Server(int logLevel, int port) throws Exception {
        serverSocket = new ServerSocket(port);
        Logger.getInstance().setLogLevel(logLevel);
    }
    
    private void log(String msg) {
        Logger.getInstance().log("Server", msg);
    }

    /**
     * Starts the server and begins accepting client connections.
     * @throws Exception
     */
    public void start() throws Exception {
        log("Server started on port " + serverSocket.getLocalPort());
        
        this.clientPool = new ClientPool();
        this.matchPool = new MatchPool(clientPool, matchDBRepository);
        this.matchPool.start();

        try {
            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                log("New client connected: " + clientSocket.getRemoteSocketAddress());
                clientPool.addClient(new ConnectedClient(clientSocket));
            }
        } catch(SocketException se) {
            if (isRunning) {
                throw se; // re-throw if not caused by server stop
            }
        }
    }

    /**
     * Starts the server asynchronously in a new thread.
     */
    public void async() {
        serverThread = new Thread(() -> {
            try {
                start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        serverThread.start();
    }

    /**
     * Stops the server and releases resources.
     * @throws Exception
     */
    public void stop() throws Exception {
        synchronized(this) {
            if (!isRunning) {
                return;
            }

            log("Shutting down server...");
            isRunning = false;
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (serverThread != null && serverThread.isAlive()) {
                serverThread.join();
            }
            if (clientPool != null && !clientPool.isShutdown()) {
                clientPool.shutdown();
            }
            if (matchPool != null) {
                matchPool.shutdown();
            }
            log("Server stopped.");
        }
    }
}
