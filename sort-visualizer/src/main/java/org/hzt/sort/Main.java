package org.hzt.sort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public final class Main {

    private static final Random rnd = new Random();
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private Thread sortingThread;

    void main() {
        LOGGER.info("Starting sort visualizer...");
        final var frame = new JFrame("Algorithm Visualizer");
        final var visualizer = new SortVisualizer(100);

        // Setup Control Panel
        final var controlPanel = new JPanel();
        final var options = new String[]{"Bubble Sort", "Quick Sort", "Merge Sort"};
        final var dropdown = new JComboBox<>(options);
        final var startButton = new JButton("Start Sort");
        final var resetButton = new JButton("Reset Array");

        controlPanel.add(new JLabel("Algorithm:"));
        controlPanel.add(dropdown);
        controlPanel.add(startButton);
        controlPanel.add(resetButton);

        // Inside your main method where other controls are:
        final var speedSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 100, 10);
        speedSlider.setInverted(true); // Left = Fast (1ms), Right = Slow (100ms)

        speedSlider.addChangeListener(_ -> visualizer.setSleepTime(speedSlider.getValue()));

        controlPanel.add(new JLabel("Speed:"));
        controlPanel.add(speedSlider);

        // Layout
        frame.setLayout(new BorderLayout());
        frame.add(controlPanel, BorderLayout.NORTH);
        frame.add(visualizer, BorderLayout.CENTER);

        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Action: Reset
        resetButton.addActionListener(_ -> {
            stopSorting();
            visualizer.shuffle(rnd); // You'll need to add this method to SortVisualizer
        });

        // Action: Start
        startButton.addActionListener(_ -> {
            LOGGER.info("Starting new sort...");
            stopSorting(); // Don't allow two sorts at once
            final var selected = (String) dropdown.getSelectedItem();

            assert selected != null;
            final var algorithm = switch (selected) {
                case "Quick Sort" -> new QuickSort(visualizer);
                case "Merge Sort" -> new MergeSort(visualizer);
                default -> new BubbleSort(visualizer);
            };

            // To run this, we wrap it in a JFrame and execute the sort in a separate Thread (so the UI doesn't freeze).
            sortingThread = Thread.ofVirtual().start(() -> algorithm.sort(visualizer.getArray()));
        });
    }

    /// Because we are using Thread.sleep() in the updateVisuals method,
    /// clicking "Start" again while a sort is running will trigger an InterruptedException.
    ///
    /// In the version above, the stopSorting() method attempts to kill the old thread so the new one can take over cleanly.
    private void stopSorting() {
        if (sortingThread != null && sortingThread.isAlive()) {
            sortingThread.interrupt();
            // In a real app, you'd add a "running" flag to the algorithms
            // to check for Thread.interrupted()
        }
    }
}
