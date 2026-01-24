package org.hzt.sort;

/// Selection: Average: $O(n^2)$, best: $O(n^2)$ A slow "scanner" moves across the array repeatedly.
public final class SelectionSort extends SortAlgorithm {
    public SelectionSort(SortVisualizer v) { super(v); }

    @Override
    public void sort(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < arr.length; j++) {
                if (compare(arr[minIdx], arr[j])) {
                    minIdx = j;
                }
                visualizer.updateVisuals(i, j);
            }
            swap(arr, minIdx, i);
        }
    }
}
