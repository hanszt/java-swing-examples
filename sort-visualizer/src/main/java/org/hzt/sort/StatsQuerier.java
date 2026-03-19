package org.hzt.sort;

/// An interface that defines the sort statistics that can be queried.
public interface StatsQuerier {
    long comparisons();
    long swaps();
    long writes();
}
