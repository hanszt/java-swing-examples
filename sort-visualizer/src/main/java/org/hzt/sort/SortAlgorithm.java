package org.hzt.sort;

/// This acts as the "plug." To add a new sort, you just extend this class and implement the sort method.
public sealed interface SortAlgorithm permits BubbleSort, HeapSort, InsertionSort, MergeSort, QuickSort, SelectionSort, TimSort {

    void sort(int[] arr);

    default String name() {
        return getClass().getSimpleName();
    }

}
