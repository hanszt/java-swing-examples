package org.hzt.sort;

@FunctionalInterface
public interface SortVisualizer {
    void updateVisuals(int pivot, int compare);

    enum ShuffleType {RANDOM, REVERSED, NEARLY_SORTED}
}

