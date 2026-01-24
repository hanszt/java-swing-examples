package org.hzt.sort;

import static org.hzt.sort.ArraysX.swap;

/// Selection: Average: $O(n^2)$, best: $O(n^2)$ A slow "scanner" moves across the array repeatedly.
public final class SelectionSort implements SortAlgorithm {

    private final SortVisualizer visualizer;
    private final ComparisonIncrementer comparisonIncrementer;
    private final SwapIncrementer swapIncrementer;

    public SelectionSort(SortVisualizer v, final ComparisonIncrementer comparisonIncrementer, final SwapIncrementer swapIncrementer) {
        this.visualizer = v;
        this.comparisonIncrementer = comparisonIncrementer;
        this.swapIncrementer = swapIncrementer;
    }

    @Override
    public void sort(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < arr.length; j++) {
                comparisonIncrementer.incrementComparison(); // Push update to UI
                if (arr[minIdx] > arr[j]) {
                    minIdx = j;
                }
                visualizer.updateVisuals(i, j);
            }
            swapIncrementer.swap(arr, minIdx, i);
            visualizer.updateVisuals(minIdx, i);
        }
    }
}
