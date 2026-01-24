package org.hzt.sort;

/// Insertion: Average $O(n^2)$, best: $O(n)$ Items "slide" into place. Very fast if nearly sorted.
public final class InsertionSort implements SortAlgorithm {
    private final SortVisualizer visualizer;
    private final ComparisonIncrementer comparisonIncrementer;
    private final SwapIncrementer swapIncrementer;

    public InsertionSort(SortVisualizer visualizer, final ComparisonIncrementer comparisonIncrementer, final SwapIncrementer swapIncrementer) {
        this.visualizer = visualizer;
        this.comparisonIncrementer = comparisonIncrementer;
        this.swapIncrementer = swapIncrementer;
    }

    @Override
    public void sort(int[] arr) {
        sort(arr, 0, arr.length - 1);
    }

    void sort(int[] arr, int left, int right) {
        for (int i = left + 1; i <= right; i++) {
            int temp = arr[i];
            int j = i - 1;
            while (j >= left) {
                comparisonIncrementer.incrementComparison(); // Push update to UI
                if (arr[j] <= temp) break;
                arr[j + 1] = arr[j];
                swapIncrementer.incrementSwaps();
                visualizer.updateVisuals(i, j);
                j--;
            }
            arr[j + 1] = temp;
            visualizer.updateVisuals(j + 1, -1);
        }
    }
}