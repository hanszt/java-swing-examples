package org.hzt.sort;

import static org.hzt.sort.ArraysX.swap;

/// Selection: Average: $O(n^2)$, best: $O(n^2)$ A slow "scanner" moves across the array repeatedly.
public final class SelectionSort implements SortAlgorithm {

    private final SortVisualizer visualizer;

    public SelectionSort(SortVisualizer v) {
        this.visualizer = v;
    }

    @Override
    public void sort(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < arr.length; j++) {
                visualizer.incrementComparison(); // Push update to UI
                if (arr[minIdx] > arr[j]) {
                    minIdx = j;
                }
                visualizer.updateVisuals(i, j);
            }
            swap(arr, minIdx, i);
            visualizer.incrementSwaps();
            visualizer.updateVisuals(minIdx, i);
        }
    }
}
