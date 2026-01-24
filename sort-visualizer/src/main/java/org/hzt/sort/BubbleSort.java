package org.hzt.sort;

/// Notice how clean the algorithm logic stays. It doesn't need to know how it's being drawn; it just calls swap.
public final class BubbleSort extends SortAlgorithm {
    public BubbleSort(final SortVisualizer v) { super(v); }

    @Override
    public void sort(final int[] arr) {
        for (var i = 0; i < arr.length - 1; i++) {
            for (var j = 0; j < arr.length - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    swap(arr, j, j + 1);
                }
            }
        }
    }
}
