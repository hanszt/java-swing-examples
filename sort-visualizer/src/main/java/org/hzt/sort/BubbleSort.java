package org.hzt.sort;

import static org.hzt.sort.ArraysX.swap;

/// Notice how clean the algorithm logic stays. It doesn't need to know how it's being drawn; it just calls swap.
public final class BubbleSort implements SortAlgorithm {

    private final SortVisualizer visualizer;
    public BubbleSort(final SortVisualizer v) { this.visualizer = v; }

    @Override
    public void sort(final int[] arr) {
        for (var i = 0; i < arr.length - 1; i++) {
            for (var j = 0; j < arr.length - i - 1; j++) {
                visualizer.incrementComparison(); // Push update to UI
                if (arr[j] > arr[j + 1]) {
                    swap(arr, j, j + 1);
                    visualizer.incrementSwaps();
                    visualizer.updateVisuals(j, j + 1);
                }
            }
        }
    }
}
