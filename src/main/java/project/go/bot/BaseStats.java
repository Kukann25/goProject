package project.go.bot;

public abstract class BaseStats<S extends BaseStats<S>> implements StatsLike<S> {
    protected int n; // Number of real visits
    protected int virtualLoss; // Number of virtual losses
    protected float q; // Total value (sum of rewards), therefore average value is q / n

    public BaseStats() {
        this.n = 0;
        this.virtualLoss = 0;
        this.q = 0.0f;
    }

    @Override
    synchronized public int N() {
        return n;
    }

    @Override
    synchronized public int VirtualLoss() {
        return virtualLoss;
    }

    @Override
    synchronized public void AddQ(float q) {
        this.q += q;
    }

    @Override
    synchronized public float AvgQ() {
        return n == 0 ? 0.0f : q / n;
    }

    @Override
    synchronized public float Q() {
        return q;
    }

    @Override
    synchronized public void SetVvl(int visits, int vl) {
        this.n = visits;
        this.virtualLoss = vl;
    }

    @Override
    synchronized public javafx.util.Pair<Integer, Integer> GetVvl() {
        return new javafx.util.Pair<>(n, virtualLoss);
    }

    @Override
    synchronized public void AddVvl(int visits, int vl) {
        this.n += visits;
        this.virtualLoss += vl;
    }

    @Override
    synchronized public int RealVisits() {
        return n;
    }

    protected void copyTo(S target) {
        target.n = this.n;
        target.virtualLoss = this.virtualLoss;
        target.q = this.q;
    }
}
