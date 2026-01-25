package org.hzt.sort;

public class StatsContainer implements StatsQuerier, StatsUpdater {

    private long comparisons = 0;
    private long swaps = 0;
    private long writes = 0;

    @Override
    public long comparisons() {
        return comparisons;
    }

    @Override
    public long swaps() {
        return swaps;
    }

    @Override
    public long writes() {
        return writes;
    }

    @Override
    public void incrementComparison() {
        comparisons++;
    }

    @Override
    public void incrementSwaps() {
        swaps++;
    }

    @Override
    public void incrementWrites() {
        writes++;
    }

    public void reset() {
        comparisons = 0;
        swaps = 0;
        writes = 0;
    }
}
