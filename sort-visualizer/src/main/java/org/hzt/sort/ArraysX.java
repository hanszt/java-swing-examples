package org.hzt.sort;

public final class ArraysX {

    private ArraysX() {
        throw new AssertionError("Cannot instantiate ArraysX");
    }

    public static void swap(final int[] arr, final int i, final int j) {
        final var temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
