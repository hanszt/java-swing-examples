package org.hzt.sort;

/// Insertion: Average $O(n^2)$, best: $O(n)$ Items "slide" into place. Very fast if nearly sorted.
public final class InsertionSort extends SortAlgorithm {
    public InsertionSort(SortVisualizer v) { super(v); }

    @Override
    public void sort(int[] arr) {
        sort(arr, 0, arr.length - 1);
    }

    void sort(int[] arr, int left, int right) {
        for (int i = left + 1; i <= right; i++) {
            int temp = arr[i];
            int j = i - 1;
            while (j >= left && compare(arr[j], temp)) {
                arr[j + 1] = arr[j];
                visualizer.incrementSwaps();
                visualizer.updateVisuals(i, j);
                j--;
            }
            arr[j + 1] = temp;
            visualizer.updateVisuals(j + 1, -1);
        }
    }
}