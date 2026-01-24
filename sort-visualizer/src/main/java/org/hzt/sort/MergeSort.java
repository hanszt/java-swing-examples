package org.hzt.sort;

/// MergeSort is unique because it doesn't typically swap elements in place; it creates temporary arrays and merges them back.
///
/// To visualize this, we modify our SortAlgorithm or simply call updateVisuals whenever we write a value back to the original array.
public final class MergeSort extends SortAlgorithm {
    public MergeSort(final SortVisualizer v) { super(v); }

    @Override
    public void sort(final int[] arr) {
        mergeSort(arr, 0, arr.length - 1);
    }

    private void mergeSort(final int[] arr, final int l, final int r) {
        if (l < r) {
            final var m = l + (r - l) / 2;
            mergeSort(arr, l, m);
            mergeSort(arr, m + 1, r);
            merge(arr, l, m, r);
        }
    }

    void merge(final int[] arr, final int l, final int m, final int r) {
        final var n1 = m - l + 1;
        final var n2 = r - m;

        final var left = new int[n1];
        final var right = new int[n2];

        System.arraycopy(arr, l, left, 0, n1);
        System.arraycopy(arr, (m + 1), right, 0, n2);

        int i = 0;
        int j = 0;
        var k = l;
        while (i < n1 && j < n2) {
            if (left[i] <= right[j]) {
                arr[k] = left[i];
                i++;
            } else {
                arr[k] = right[j];
                j++;
            }
            visualizer.updateVisuals(k, -1); // Visualize the write-back
            k++;
        }

        while (i < n1) {
            arr[k] = left[i];
            visualizer.updateVisuals(k, -1);
            i++; k++;
        }
        while (j < n2) {
            arr[k] = right[j];
            visualizer.updateVisuals(k, -1);
            j++; k++;
        }
    }
}
