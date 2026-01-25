package org.hzt.sort;

@FunctionalInterface
public interface SwapIncrementer {
    void incrementSwaps();

    default void swap(final int[] arr, final int i, final int j) {
        final var temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
        incrementSwaps();
    }
}
