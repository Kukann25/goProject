package project.go.bot;

public class TreeStats {
    private int maxDepth;
    private int sps; // simulations per second
    private int simulations;

    public TreeStats() {
        this.maxDepth = 0;
        this.sps = 0;
        this.simulations = 0;
    }

    /**
     * Reset the tree statistics.
     */
    public synchronized void reset() {
        this.maxDepth = 0;
        this.sps = 0;
        this.simulations = 0;
    }

    /**
     * Update the maximum depth reached in the tree.
     * @param depth The depth to compare with the current maximum depth.
     */
    public synchronized void updateMaxDepth(int depth) {
        if (depth > this.maxDepth) {
            this.maxDepth = depth;
        }
    }

    /**
     * Update simulations per second based on elapsed time.
     * @param elapsedMillis The elapsed time in milliseconds.
     */
    public void updateSps(int elapsedMillis) {
        if (elapsedMillis > 0) {
            this.sps = (this.simulations * 1000) / elapsedMillis;
        }
    }

    /**
     * Get the maximum depth reached in the tree.
     * @return The maximum depth.
     */
    public synchronized int getMaxDepth() {
        return this.maxDepth;
    }

    /**
     * Get the simulations per second.
     * @return The simulations per second.
     */
    public synchronized int getSps() {
        return this.sps;
    }

    /**
     * Get the total number of simulations performed.
     * @return The total simulations.
     */
    public synchronized int getSimulations() {
        return this.simulations;
    }

    /**
     * Increment the total number of simulations performed by one.
     */
    public synchronized void incrementSimulations() {
        this.simulations++;
    }
}
