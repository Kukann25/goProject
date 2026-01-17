package project.go.bot;

public class Limiter {
    private volatile boolean stopped = false;
    private volatile boolean allowExpansion = true;
    private Limits limits;
    private long startTimeMillis;

    public Limiter() {
        this.limits = new Limits(0, 0); // No limits by default
    }

    public Limiter(Limits limits) {
        this.limits = limits;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void stop() {
        this.stopped = true;
    }

    public boolean isAllowExpansion() {
        return allowExpansion;
    }

    public void setAllowExpansion(boolean allowExpansion) {
        this.allowExpansion = allowExpansion;
    }

    public void start() {
        this.startTimeMillis = System.currentTimeMillis();
        this.stopped = false;
        this.allowExpansion = true;
    }

    public boolean isOk(int simulations, int depth) {
        if (limits.getMaxSimulations() > 0 && simulations >= limits.getMaxSimulations()) {
            return false;
        }
        if (limits.getMaxTimeMillis() > 0) {
            long elapsed = System.currentTimeMillis() - startTimeMillis;
            if (elapsed >= limits.getMaxTimeMillis()) {
                return false;
            }
        }
        return true;
    }
}
