package org.hzt.sort;

import org.hzt.sort.SortVisualizer.ShuffleType;
import org.hzt.sort.swing.ConfigurableListCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public final class Main {

    private static final Random rnd = new Random();
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private Thread sortingThread = null;
    private final JComboBox<SortAlgorithm> sortAlgorithmDropdown = new JComboBox<>();
    private final JComboBox<ShuffleType> shuffleDropdown = new JComboBox<>(ShuffleType.values());

    void main() {
        LOGGER.info("Starting sort visualizer...");
        final var frame = new JFrame("Algorithm Visualizer");
        final var visualizer = new JSortVisualizer(100, rnd);

        // Setup Control Panel
        final var  controlPanel = new JPanel();
        final var options = new SortAlgorithm[]{
                new BubbleSort(visualizer, visualizer, visualizer),
                new SelectionSort(visualizer, visualizer, visualizer),
                new InsertionSort(visualizer, visualizer, visualizer),
                new QuickSort(visualizer, visualizer, visualizer),
                new MergeSort(visualizer),
                new TimSort(visualizer, visualizer, visualizer)
        };
        sortAlgorithmDropdown.addActionListener(_ -> setupSort(visualizer));
        sortAlgorithmDropdown.setModel(new DefaultComboBoxModel<>(options));
        sortAlgorithmDropdown.setRenderer(new ConfigurableListCellRenderer<>(SortAlgorithm::name));
        final var startButton = new JButton("Start Sort");
        final var resetButton = new JButton("Reset Array");
        JCheckBox soundToggle = new JCheckBox("Sound", true);
        soundToggle.addActionListener(_ -> visualizer.setSoundEnabled(soundToggle.isSelected()));
        controlPanel.add(soundToggle);

        final var dropdownPanel = new JPanel(new GridLayout(2, 2));
        dropdownPanel.add(new JLabel("Algorithm:"));
        dropdownPanel.add(sortAlgorithmDropdown);
        dropdownPanel.add(new JLabel("Data Type:"));
        dropdownPanel.add(shuffleDropdown);

        final var buttonPanel = new JPanel(new GridLayout(2, 2));
        buttonPanel.add(startButton);
        buttonPanel.add(resetButton);

        controlPanel.add(dropdownPanel);
        controlPanel.add(buttonPanel);
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
        shuffleDropdown.addActionListener(_ -> reset(visualizer));
        shuffleDropdown.setRenderer(new ConfigurableListCellRenderer<>((ShuffleType st) -> {
            final var lowerCase = st.name().replace("_", " ").toLowerCase();
            return Character.toString(lowerCase.charAt(0)).toUpperCase() + lowerCase.substring(1);
        }));
        resetButton.addActionListener(_ -> reset(visualizer));
    }

    private void setupSort(final JSortVisualizer visualizer) {
        reset(visualizer);
        final var selected = (SortAlgorithm) sortAlgorithmDropdown.getSelectedItem();
        assert selected != null;
        LOGGER.atInfo().setMessage(() -> "Going to use sort algorithm %s...".formatted(selected.name())).log();

        // To run this, we wrap it in a JFrame and execute the sort in a separate Thread (so the UI doesn't freeze).
        sortingThread = unstartedSortingThread(visualizer, selected);
    }

    private Thread unstartedSortingThread(final JSortVisualizer visualizer, final SortAlgorithm algo) {
        return Thread.ofVirtual().unstarted(() -> {
            visualizer.setValidationIndex(-1); // Reset validation
            algo.sort(visualizer.getArray());

            // The Validation Sweep
            visualizer.doValidationSweep();
        });
    }

    private void reset(final JSortVisualizer visualizer) {
        stopSorting();
        visualizer.reset();
        visualizer.shuffle((ShuffleType) shuffleDropdown.getSelectedItem());
        sortingThread = unstartedSortingThread(visualizer, (SortAlgorithm) sortAlgorithmDropdown.getSelectedItem());
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
