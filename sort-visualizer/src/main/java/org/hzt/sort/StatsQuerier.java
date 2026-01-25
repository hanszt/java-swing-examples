package org.hzt.sort;

public interface StatsQuerier {
    long comparisons();
    long swaps();
    long writes();
}
