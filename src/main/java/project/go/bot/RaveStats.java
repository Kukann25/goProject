package project.go.bot;

public class RaveStats extends BaseStats<RaveStats> {
    protected float raveQ; // Total RAVE value (sum of RAVE rewards), therefore average RAVE value is raveQ / n
    protected int raveN; // Number of RAVE visits

    public RaveStats() {
        super();
        this.raveQ = 0.0f;
        this.raveN = 0;
    }

    synchronized public void AddRaveQ(float q) {
        this.raveQ += q;
    }

    synchronized public float AvgRaveQ() {
        return raveN == 0 ? 0.0f : raveQ / raveN;
    }

    synchronized public float RaveQ() {
        return raveQ;
    }

    synchronized public void SetRaveN(int n) {
        this.raveN = n;
    }

    synchronized public int RaveN() {
        return raveN;
    }

    @Override
    synchronized public RaveStats Clone() {
        RaveStats clone = new RaveStats();
        copyTo(clone);
        clone.raveQ = this.raveQ;
        clone.raveN = this.raveN;
        return clone;
    }
}
