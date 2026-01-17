package project.go.bot;

public class Limits {
    private int maxSimulations;
    private long maxTimeMillis;

    public Limits(int maxSimulations, long maxTimeMillis) {
        this.maxSimulations = maxSimulations;
        this.maxTimeMillis = maxTimeMillis;
    }

    public int getMaxSimulations() {
        return maxSimulations;
    }

    public long getMaxTimeMillis() {
        return maxTimeMillis;
    }
}
