package project.go.server.client;

/**
 * A thread-safe printer for synchronized console output.
 */
public final class SyncPrinter {
    private static final Object lock = new Object();
    private static final String INFO_FMT = "\033[1;34m%s\033[0m\n";
    private static final String ERROR_FMT = "\033[1;31m%s\033[0m\n";
    private static final String SUCCESS_FMT = "\033[1;32m%s\033[0m\n";
    private static final String DETAIL_FMT = "\033[0;36m%s\033[0m\n";



    public static void println(String message) {
        synchronized (lock) {
            System.out.println(message);
        }
    }

    public static void print(String message) {
        synchronized (lock) {
            System.out.print(message);
        }
    }


    public static void detail(String msg) {
        synchronized (lock) {
            System.out.printf(DETAIL_FMT, msg);
        }
    }

    /**
     * Print an informational message (blue).
     */
    public static void info(String msg) {
        synchronized (lock) {
            System.out.printf(INFO_FMT, msg);
        }
    }

    /**
     * Print an error message (red).
     */
    public static void error(String msg) {
        synchronized (lock) {
            System.out.printf(ERROR_FMT, msg);
        }
    }

    /**
     * Print a success message (green).
     */
    public static void success(String msg) {
        synchronized (lock) {
            System.out.printf(SUCCESS_FMT, msg);
        }
    }
}
