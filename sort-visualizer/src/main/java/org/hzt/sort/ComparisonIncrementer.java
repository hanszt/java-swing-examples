package org.hzt.sort;

@FunctionalInterface
public interface ComparisonIncrementer {
    void incrementComparison();

    default boolean compare(int a, int b) {
        incrementComparison();
        return a > b;
    }
}
