package org.hzt.sort;

@FunctionalInterface
public interface SwapIncrementer {
    void incrementSwaps();

    default void swap(final int[] arr, final int i, final int j) {
        ArraysX.swap(arr, i, j);
        incrementSwaps();
    }
}
