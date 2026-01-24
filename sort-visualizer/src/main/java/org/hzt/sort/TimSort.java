package org.hzt.sort;

/// It is the default sorting algorithm for Java (Arrays.sort) and Python.
///
/// It’s a "hybrid" algorithm, meaning it combines the best of both worlds:
///
/// it uses Insertion Sort for small chunks (called "runs") and Merge Sort to combine those chunks.
///
/// Because it is highly optimized for real-world data, it is significantly more complex to code than the others,
/// but it looks fascinating when visualized—you'll see small sections being sorted locally before a massive merge sweep happens.
public final class TimSort implements SortAlgorithm {
    private static final int RUN = 16;
    private final MergeSort mergeSort;
    private final InsertionSort insertionSort;

    public TimSort(SortVisualizer v,
                   ComparisonIncrementer comparisonIncrementer,
                   SwapIncrementer swapIncrementer
    ) {
        mergeSort = new MergeSort(v);
        insertionSort = new InsertionSort(v, comparisonIncrementer, swapIncrementer);
    }

    @Override
    public void sort(int[] arr) {
        int n = arr.length;

        // 1. Sort individual subarrays of size RUN using Insertion Sort
        for (int i = 0; i < n; i += RUN) {
            insertionSort.sort(arr, i, Math.min((i + RUN - 1), (n - 1)));
        }

        // 2. Start merging from size RUN
        for (int size = RUN; size < n; size = 2 * size) {
            for (int left = 0; left < n; left += 2 * size) {
                int mid = left + size - 1;
                int right = Math.min((left + 2 * size - 1), (n - 1));

                if (mid < right) {
                    mergeSort.merge(arr, left, mid, right);
                }
            }
        }
    }
}
