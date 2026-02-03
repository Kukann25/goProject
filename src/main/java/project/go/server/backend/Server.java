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
    private com.sun.net.httpserver.HttpServer httpServer;

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
        
        try {
            httpServer = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(8080), 0);
            
            httpServer.createContext("/histories", exchange -> {
                if ("GET".equals(exchange.getRequestMethod())) {
                    // Return all match histories
                    if (matchDBRepository != null) {
                        try {
                            java.util.List<project.go.dbinterface.DBMatch> matches = matchDBRepository.findAll();
                            project.go.server.common.json.MatchHistoryResponse resp = new project.go.server.common.json.MatchHistoryResponse();
                            if (matches != null) {
                                for(project.go.dbinterface.DBMatch m : matches) {
                                    java.util.List<project.go.server.common.json.MatchHistoryResponse.Move> moves = new java.util.ArrayList<>();
                                    if(m.getMoves() != null) {
                                        for(project.go.dbinterface.DBMove mv : m.getMoves()) {
                                            moves.add(new project.go.server.common.json.MatchHistoryResponse.Move(mv.getColor(), mv.getX(), mv.getY()));
                                        }
                                    }
                                    resp.addMatch(new project.go.server.common.json.MatchHistoryResponse.Match(m.getId(), m.getPlayerBlack(), m.getPlayerWhite(), moves));
                                }
                            }
                            String json = project.go.server.common.json.JsonFmt.toJson(resp);
                            exchange.getResponseHeaders().set("Content-Type", "application/json");
                            exchange.sendResponseHeaders(200, json.length());
                            try (java.io.OutputStream os = exchange.getResponseBody()) {
                                os.write(json.getBytes());
                            }
                        } catch (Exception e) {
                            String msg = "Internal error: " + e.getMessage();
                            exchange.sendResponseHeaders(500, msg.length());
                            try (java.io.OutputStream os = exchange.getResponseBody()) { os.write(msg.getBytes()); }
                        }
                    } else {
                        String msg = "Match repository not available";
                        exchange.sendResponseHeaders(400, msg.length());
                        try (java.io.OutputStream os = exchange.getResponseBody()) {
                            os.write(msg.getBytes());
                        }
                    }
                }
            });
             
            httpServer.createContext("/history", exchange -> {
                 if ("GET".equals(exchange.getRequestMethod())) {
                     String query = exchange.getRequestURI().getQuery();
                     String matchId = null;
                     if (query != null) {
                         for (String param : query.split("&")) {
                             String[] pair = param.split("=");
                             if (pair.length > 1 && "matchId".equals(pair[0])) {
                                 matchId = pair[1];
                                 break;
                             }
                         }
                     }

                     if (matchId != null && matchDBRepository != null) {
                         try {
                             java.util.Optional<project.go.dbinterface.DBMatch> match = matchDBRepository.findById(matchId);
                             project.go.server.common.json.MatchHistoryResponse resp = new project.go.server.common.json.MatchHistoryResponse();
                             if (match.isPresent()) {
                                 project.go.dbinterface.DBMatch m = match.get();
                                 java.util.List<project.go.server.common.json.MatchHistoryResponse.Move> moves = new java.util.ArrayList<>();
                                 if(m.getMoves() != null) {
                                     for(project.go.dbinterface.DBMove mv : m.getMoves()) {
                                         moves.add(new project.go.server.common.json.MatchHistoryResponse.Move(mv.getColor(), mv.getX(), mv.getY()));
                                     }
                                 }
                                 resp.addMatch(new project.go.server.common.json.MatchHistoryResponse.Match(m.getId(), m.getPlayerBlack(), m.getPlayerWhite(), moves));
                             } else {
                                 String msg = "Match not found";
                                 exchange.sendResponseHeaders(404, msg.length());
                                 try (java.io.OutputStream os = exchange.getResponseBody()) {
                                     os.write(msg.getBytes());
                                 }
                                 return;
                             }
                             
                             String json = project.go.server.common.json.JsonFmt.toJson(resp);
                             exchange.getResponseHeaders().set("Content-Type", "application/json");
                             exchange.sendResponseHeaders(200, json.length());
                             try (java.io.OutputStream os = exchange.getResponseBody()) {
                                 os.write(json.getBytes());
                             }
                         } catch (Exception e) {
                             String msg = "Internal error: " + e.getMessage();
                             exchange.sendResponseHeaders(500, msg.length());
                             try (java.io.OutputStream os = exchange.getResponseBody()) { os.write(msg.getBytes()); }
                         }
                     } else {
                         String msg = "Match repository not available";
                         exchange.sendResponseHeaders(400, msg.length());
                         try (java.io.OutputStream os = exchange.getResponseBody()) {
                             os.write(msg.getBytes());
                         }
                     }
                 } else {
                     exchange.sendResponseHeaders(405, -1);
                 }
             });
             httpServer.start();
             log("HTTP Server started on port 8080");
        } catch (Exception e) {
            log("Failed to start HTTP server: " + e.getMessage());
        }

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
            if (httpServer != null) {
                httpServer.stop(0);
            }
            log("Server stopped.");
        }
    }
}
