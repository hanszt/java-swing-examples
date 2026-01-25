package org.hzt.sort;

/// Notice how clean the algorithm logic stays. It doesn't need to know how it's being drawn; it just calls swap.
public final class BubbleSort implements SortAlgorithm {

    private final SortVisualizer visualizer;
    private final ComparisonIncrementer comparisonIncrementer;
    private final SwapIncrementer swapIncrementer;

    public BubbleSort(
            final SortVisualizer v,
            final ComparisonIncrementer comparisonIncrementer,
            final SwapIncrementer swapIncrementer) {
        this.visualizer = v;
        this.comparisonIncrementer = comparisonIncrementer;
        this.swapIncrementer = swapIncrementer;
    }

    @Override
    public void sort(final int[] arr) {
        for (var i = 0; i < arr.length - 1; i++) {
            for (var j = 0; j < arr.length - i - 1; j++) {
                visualizer.updateVisuals(j, j + 1);
                if (comparisonIncrementer.compare(arr[j], arr[j + 1])) {
                    swapIncrementer.swap(arr, j, j + 1);
                }
            }
        }
    }
}
