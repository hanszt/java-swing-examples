package org.hzt.sort;

@FunctionalInterface
public interface SortVisualizer {
    void updateVisuals(int i, int j);

    enum ShuffleType {RANDOM, REVERSED, NEARLY_SORTED}
}

