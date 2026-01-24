package org.hzt.sort;

/// Insertion: Average $O(n^2)$, best: $O(n)$ Items "slide" into place. Very fast if nearly sorted.
public final class InsertionSort extends SortAlgorithm {
    public InsertionSort(SortVisualizer v) { super(v); }

    @Override
    public void sort(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            int key = arr[i];
            int j = i - 1;

            // Visualize the key being picked up
            while (j >= 0 && compare(arr[j], key)) {
                arr[j + 1] = arr[j];
                visualizer.incrementSwaps();
                visualizer.updateVisuals(i, j);
                j = j - 1;
            }
            arr[j + 1] = key;
            visualizer.updateVisuals(j + 1, -1);
        }
    }
}