package org.hzt.sort;

import javax.swing.*;
import java.awt.*;
import java.util.random.RandomGenerator;

final class JSortVisualizer extends JPanel implements SortVisualizer {
    private final int[] array;
    private int currentPivot = -1;
    private int currentCompare = -1;
    private volatile int sleepTime = 10; // Default speed
    private int validationIndex = -1;

    private long comparisons = 0;
    private long swaps = 0;

    JSortVisualizer(final int size, RandomGenerator rand) {
        array = new int[size];
        for (var i = 0; i < size; i++) array[i] = rand.nextInt(400) + 10;
        setBackground(Color.BLACK);
    }

    public void setSleepTime(final int ms) {
        this.sleepTime = ms;
    }

    public void setValidationIndex(int index) {
        this.validationIndex = index;
        repaint();
    }

    // This is where the magic happens: any sort can call this
    public void updateVisuals(final int pivot, final int compare) {
        this.currentPivot = pivot;
        this.currentCompare = compare;
        try {
            Thread.sleep(sleepTime); // Control the speed here
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
        repaint();
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        final var barWidth = getWidth() / array.length;
        for (var i = 0; i < array.length; i++) {
            if (i <= validationIndex) g.setColor(Color.GREEN); // Sorted & Verified
            else if (i == currentPivot) g.setColor(Color.RED);
            else if (i == currentCompare) g.setColor(Color.YELLOW);
            else g.setColor(Color.WHITE);

            g.fillRect(i * barWidth, getHeight() - array[i], barWidth - 1, array[i]);
        }
        drawStats(g);
    }

    private void drawStats(final Graphics g) {
        // Draw the stats in the top left corner
        g.setColor(Color.GREEN);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.drawString("Comparisons: " + comparisons, 20, 30);
        g.drawString("Swaps:       " + swaps, 20, 50);
    }

    public int[] getArray() {
        return array;
    }

    public void shuffle(RandomGenerator rand) {
        for (var i = 0; i < array.length; i++) {
            array[i] = rand.nextInt(400) + 10;
        }
        currentPivot = -1;
        currentCompare = -1;
        repaint();
    }

    public void incrementComparison() {
        this.comparisons++;
    }

    public void incrementSwaps() {
        swaps++;
    }

    public void reset() {
        updateVisuals(-1, -1);
        comparisons = 0;
        swaps = 0;
        setValidationIndex(-1); // Reset validation
    }

    @Override
    public String toString() {
        return "JSortVisualizer{" +
                "currentPivot=" + currentPivot +
                ", currentCompare=" + currentCompare +
                ", sleepTime=" + sleepTime +
                ", validationIndex=" + validationIndex +
                ", comparisons=" + comparisons +
                ", swaps=" + swaps +
                '}';
    }
}
