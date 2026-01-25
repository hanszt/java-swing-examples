package org.hzt.sort;

public final class HeapSort implements SortAlgorithm {

    private final SortVisualizer visualizer;
    private final StatsUpdater statsUpdater;

    public HeapSort(final SortVisualizer v, final StatsUpdater statsUpdater) {
        this.visualizer = v;
        this.statsUpdater = statsUpdater;
    }

    @Override
    public void sort(int[] arr) {
        int n = arr.length;

        // 1. Build the Max Heap
        // We start from the last non-leaf node and work upwards
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i);
        }

        // 2. Extract elements from the heap one by one
        for (int i = n - 1; i > 0; i--) {
            // Move current root (largest) to the end
            statsUpdater.swap(arr, 0, i);

            // Call max heapify on the reduced heap
            heapify(arr, i, 0);
        }
    }

    private void heapify(int[] arr, int n, int i) {
        int largest = i; // Initialize largest as root
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        // If left child is larger than root
        if (left < n) {
            visualizer.updateVisuals(i, left);
            if (statsUpdater.compare(arr[left], arr[largest])) {
                largest = left;
            }
        }

        // If right child is larger than largest so far
        if (right < n) {
            visualizer.updateVisuals(largest, right);
            if (statsUpdater.compare(arr[right], arr[largest])) {
                largest = right;
            }
        }

        // If largest is not root, swap and continue heapifying
        if (largest != i) {
            statsUpdater.swap(arr, i, largest);
            heapify(arr, n, largest);
        }
    }
}
