package org.hzt.sort;

import java.awt.*;

enum ColorMode {

    WHITE(Color.RED, Color.YELLOW) {
        @Override
        Color getColor(final int value) {
            return Color.WHITE;
        }
    },

    RAINBOW(Color.WHITE, Color.GRAY) {
        @Override
        Color getColor(final int value) {
            // Map value (10-460) to Hue (0.0 to 0.8)
            float hue = (value - 10) / 450f;
            return Color.getHSBColor(hue, 0.8f, 0.9f);
        }
    };

    private final Color pivotColor;
    private final Color compareColor;

    ColorMode(final Color pivotColor, final Color compareColor) {
        this.pivotColor = pivotColor;
        this.compareColor = compareColor;
    }

    abstract Color getColor(int index);
    Color getPivotColor() { return pivotColor; }
    Color getCompareColor() { return compareColor; }
}
