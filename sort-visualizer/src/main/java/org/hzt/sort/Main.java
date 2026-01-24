package org.hzt.sort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public final class Main {

    private static final Random rnd = new Random();
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private Thread sortingThread = null;
    private final JComboBox<SortAlgorithm> dropdown = new JComboBox<>();

    void main() {
        LOGGER.info("Starting sort visualizer...");
        final var frame = new JFrame("Algorithm Visualizer");
        final var visualizer = new SortVisualizer(100, rnd);

        // Setup Control Panel
        final var controlPanel = new JPanel();
        final var options = new SortAlgorithm[]{
                new BubbleSort(visualizer),
                new QuickSort(visualizer),
                new MergeSort(visualizer),
                new SelectionSort(visualizer),
                new InsertionSort(visualizer)
        };
        dropdown.setModel(new DefaultComboBoxModel<>(options));
        dropdown.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {

                // Call the super method to handle background colors/selection logic
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof final SortAlgorithm algo) {
                    setText(algo.name()); // This sets the display text to the enum name
                }
                return this;
            }
        });
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

        // Initial setup on startup
        setupSort(visualizer);

        // Action: Reset
        resetButton.addActionListener(_ -> reset(visualizer));

        // Action: Start
        startButton.addActionListener(_ -> {
            if (sortingThread != null) {
                final var state = sortingThread.getState();
                if (state == Thread.State.NEW) {
                    sortingThread.start();
                }
            }
        });
        dropdown.addActionListener(_ -> setupSort(visualizer));
    }

    private void setupSort(final SortVisualizer visualizer) {
        reset(visualizer);
        final var selected = (SortAlgorithm) dropdown.getSelectedItem();
        assert selected != null;
        LOGGER.atInfo().setMessage(() -> "Going to use sort algorithm %s...".formatted(selected.name())).log();

        // To run this, we wrap it in a JFrame and execute the sort in a separate Thread (so the UI doesn't freeze).
        sortingThread = unstartedSortingThread(visualizer, selected);
    }

    private Thread unstartedSortingThread(final SortVisualizer visualizer, final SortAlgorithm algo) {
        return Thread.ofVirtual().unstarted(() -> {
            visualizer.setValidationIndex(-1); // Reset validation
            algo.sort(visualizer.getArray());

            // The Validation Sweep
            doValidationSweep(visualizer);
        });
    }

    private void reset(final SortVisualizer visualizer) {
        stopSorting();
        visualizer.reset();
        visualizer.shuffle(rnd);
        sortingThread = unstartedSortingThread(visualizer, (SortAlgorithm) dropdown.getSelectedItem());
    }

    private void doValidationSweep(final SortVisualizer visualizer) {
        for (int i = 0; i < visualizer.getArray().length; i++) {
            visualizer.setValidationIndex(i);
            try {
                Thread.sleep(5);
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
            }
        }
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
