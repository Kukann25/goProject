package project.go.server.backend;

/**
 * Global logger singleton
 */
public class Logger {

    // Available log levels
    public static final int LEVEL_INFO = 1;
    public static final int LEVEL_ERROR = 2;
    public static final int LEVEL_ALL = LEVEL_INFO | LEVEL_ERROR;

    // Singleton instance
    private static Logger instance = null;
    private int logLevel = LEVEL_ALL;
    

    private Logger() {
        // private constructor to prevent instantiation
    }

    // Double-checked locking for thread-safe singleton
    public static Logger getInstance() {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger();
                }
            }
        }
        return instance;
    }

    public void setLogLevel(int level) {
        this.logLevel = level;
    }

    public void log(String message) {
        if ((logLevel & LEVEL_INFO) == 0) return;
        System.out.println("[LOG] " + message);
    }
    
    public void log(String tag, String message) {
        if ((logLevel & LEVEL_INFO) == 0) return;
        System.out.println("[" + tag + "] " + message);
    }

    public void error(String message) {
        if ((logLevel & LEVEL_ERROR) == 0) return;
        System.err.println("[ERROR] " + message);
    }

    public void error(String tag, String message) {
        if ((logLevel & LEVEL_ERROR) == 0) return;
        System.err.println("[" + tag + "][ERROR] " + message);
    }
}

