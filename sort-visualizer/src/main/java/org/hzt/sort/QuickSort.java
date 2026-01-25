package org.hzt.sort;

/// Implementing recursive algorithms like QuickSort is where this visualizer really shines, as you can see the "Divide and Conquer" strategy in action.
///
/// QuickSort is a bit more complex than Bubble Sort because it relies on a pivot to partition the array. In our visualizer, the bars will dance around that pivot until the entire array is sorted.
public final class QuickSort implements SortAlgorithm {

    private final SortVisualizer visualizer;
    private final ComparisonIncrementer comparisonIncrementer;
    private final SwapIncrementer swapIncrementer;

    public QuickSort(final SortVisualizer v,
                     final ComparisonIncrementer comparisonIncrementer,
                     final SwapIncrementer swapIncrementer) {
        this.visualizer = v;
        this.comparisonIncrementer = comparisonIncrementer;
        this.swapIncrementer = swapIncrementer;
    }

    @Override
    public void sort(final int[] arr) {
        quickSort(arr, 0, arr.length - 1);
    }

    private void quickSort(final int[] arr, final int low, final int high) {
        if (low < high) {
            final var pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }

    private int partition(final int[] arr, final int low, final int high) {
        final var pivot = arr[high];
        var i = (low - 1);
        for (var j = low; j < high; j++) {
            // Visualize the comparison
            visualizer.updateVisuals(high, j);

            comparisonIncrementer.incrementComparison(); // Push update to UI
            if (pivot > arr[j]) {
                i++;
                swapIncrementer.swap(arr, i, j);
                visualizer.updateVisuals(i, j);
            }
        }
        swapIncrementer.swap(arr, i + 1, high);
        visualizer.updateVisuals(i + 1, high);
        return i + 1;
    }
}
