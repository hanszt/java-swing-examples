package org.hzt.sort;

/// This acts as the "plug." To add a new sort, you just extend this class and implement the sort method.
public abstract class SortAlgorithm {
    protected SortVisualizer visualizer;

    protected SortAlgorithm(final SortVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    public abstract void sort(int[] arr);

    protected void swap(final int[] arr, final int i, final int j) {
        final var temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
        visualizer.incrementSwaps();
        visualizer.updateVisuals(i, j);
    }

    protected boolean compare(int a, int b) {
        visualizer.incrementComparison(); // Push update to UI
        return a > b;
    }
}
