package org.hzt.sort.swing;

import org.hzt.sort.*;

import javax.swing.*;
import java.awt.*;
import java.util.random.RandomGenerator;

final class JSortVisualizer extends JPanel implements SortVisualizer, StatsUpdater {

    private final transient SoundEngine soundEngine = new SoundEngine();
    private final transient RandomGenerator rand;

    private final int[] array;
    private int currentPivot = -1;
    private int currentCompare = -1;
    private volatile int sleepTime = 10; // Default speed
    private int validationIndex = -1;
    private boolean soundEnabled = true;
    private ColorMode colorMode = ColorMode.WHITE;
    private transient String sortAlgorithmName = "-";

    private long comparisons = 0;
    private long swaps = 0;
    private long writes = 0;

    JSortVisualizer(final int size, RandomGenerator rand) {
        this.rand = rand;
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

    public void setSortAlgorithmName(final String sortAlgorithmName) {
        this.sortAlgorithmName = sortAlgorithmName;
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public void setColorMode(final ColorMode colorMode) {
        this.colorMode = colorMode;
        repaint();
    }

    // This is where the magic happens: any sort can call this
    public void updateVisuals(final int pivot, final int compare) {
        this.currentPivot = pivot;
        this.currentCompare = compare;

        // Play sound based on the element being acted upon
        if (soundEnabled && pivot >= 0 && compare < array.length) {
            soundEngine.playNote(array[pivot]);
        }
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
            else if (i == currentPivot) g.setColor(colorMode.getPivotColor());
            else if (i == currentCompare) g.setColor(colorMode.getCompareColor());
            else g.setColor(colorMode.getColor(array[i]));

            g.fillRect(i * barWidth, getHeight() - array[i], barWidth - 1, array[i]);
        }
        drawStats(g);
    }

    void doValidationSweep() {
        for (int i = 0; i < array.length; i++) {
            setValidationIndex(i);
            if (soundEnabled) {
                soundEngine.playNote(array[i]);
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void drawStats(final Graphics g) {
        // Draw the stats in the top left corner
        g.setColor(Color.GREEN);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.drawString("Running algorithm: " + sortAlgorithmName, 20, 30);
        g.drawString("Comparisons:       " + comparisons, 20, 50);
        g.drawString("Swaps:             " + swaps, 20, 70);
        g.drawString("Writes:            " + writes, 20, 90);
    }

    public int[] getArray() {
        return array;
    }

    public void shuffle(ShuffleType type) {
        int n = array.length;

        // First, create a sorted base for certain types
        for (int i = 0; i < n; i++) array[i] = (int) (((double) i / n) * 400) + 10;

        switch (type) {
            case RANDOM -> {
                for (int i = 0; i < n; i++) {
                    int j = rand.nextInt(n);
                    int temp = array[i];
                    array[i] = array[j];
                    array[j] = temp;
                }
            }
            case REVERSED -> {
                for (int i = 0; i < n / 2; i++) {
                    int temp = array[i];
                    array[i] = array[n - 1 - i];
                    array[n - 1 - i] = temp;
                }
            }
            case NEARLY_SORTED -> {
                // Swap a few random pairs to create slight disorder
                for (int i = 0; i < 5; i++) {
                    int idx1 = rand.nextInt(n);
                    int idx2 = rand.nextInt(n);
                    int temp = array[idx1];
                    array[idx1] = array[idx2];
                    array[idx2] = temp;
                }
            }
        }
        validationIndex = -1;
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

    @Override
    public void incrementWrites() {
        writes++;
    }

    public void reset() {
        updateVisuals(-1, -1);
        comparisons = 0;
        swaps = 0;
        writes = 0;
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
                ", writes=" + writes +
                ", swaps=" + swaps +
                '}';
    }
}
