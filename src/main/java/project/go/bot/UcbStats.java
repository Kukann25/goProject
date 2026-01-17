package project.go.bot;

public class UcbStats extends BaseStats<UcbStats> {
    public UcbStats() {
        super();
    }

    @Override
    synchronized public UcbStats Clone() {
        UcbStats clone = new UcbStats();
        copyTo(clone);
        return clone;
    }
}
