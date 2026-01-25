package org.hzt.sort;

import org.hzt.sort.SortVisualizer.ShuffleType;
import org.hzt.sort.swing.ConfigurableListCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.random.RandomGenerator;

public final class Main {

    private static final RandomGenerator rnd = RandomGenerator.getDefault();
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
        sortAlgorithmDropdown.setModel(new DefaultComboBoxModel<>(options));
        sortAlgorithmDropdown.setRenderer(new ConfigurableListCellRenderer<>(SortAlgorithm::name));
        final var startButton = new JButton("Start Sort");
        final var reshuffleButton = new JButton("Reshuffle");

        final var dropdownPanel = new JPanel(new GridLayout(3, 2));
        dropdownPanel.add(new JLabel("Algorithm:"));
        dropdownPanel.add(sortAlgorithmDropdown);
        dropdownPanel.add(new JLabel("Shuffle Type:"));
        dropdownPanel.add(shuffleDropdown);
        final var colorModeDropdown = new JComboBox<>(ColorMode.values());
        colorModeDropdown.setRenderer(new ConfigurableListCellRenderer<>(this::enumNameToFirstLetterUppercase));
        dropdownPanel.add(new JLabel("Color Mode:"));
        dropdownPanel.add(colorModeDropdown);
        colorModeDropdown.addActionListener(_ -> visualizer.setColorMode((ColorMode) colorModeDropdown.getSelectedItem()));

        final var buttonPanel = new JPanel(new GridLayout(3, 2));
        final var soundToggle = new JCheckBox("Sound", true);
        soundToggle.addActionListener(_ -> visualizer.setSoundEnabled(soundToggle.isSelected()));
        buttonPanel.add(startButton);
        buttonPanel.add(reshuffleButton);
        buttonPanel.add(soundToggle);

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
        reshuffleButton.addActionListener(_ -> reset(visualizer));

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
        shuffleDropdown.setRenderer(new ConfigurableListCellRenderer<>(this::enumNameToFirstLetterUppercase));
        reshuffleButton.addActionListener(_ -> reset(visualizer));
    }

    private String enumNameToFirstLetterUppercase(final Enum<?> name) {
        final var lowerCase = name.name().replace("_", " ").toLowerCase();
        return Character.toString(lowerCase.charAt(0)).toUpperCase() + lowerCase.substring(1);
    }

    private void setupSort(final JSortVisualizer visualizer) {
        reset(visualizer);
        // To run this, we wrap it in a JFrame and execute the sort in a separate Thread (so the UI doesn't freeze).
        sortingThread = unstartedSortingThread(visualizer);
    }

    private Thread unstartedSortingThread(final JSortVisualizer visualizer) {
        return Thread.ofVirtual().unstarted(() -> {
            final var selected = (SortAlgorithm) sortAlgorithmDropdown.getSelectedItem();
            assert selected != null;
            LOGGER.atInfo().setMessage(() -> "Going to use sort algorithm %s...".formatted(selected.name())).log();
            visualizer.setValidationIndex(-1); // Reset validation
            visualizer.setSortAlgorithmName(selected.name());

            selected.sort(visualizer.getArray());
            // The Validation Sweep
            visualizer.doValidationSweep();
        });
    }

    private void reset(final JSortVisualizer visualizer) {
        stopSorting();
        if (sortingThread != null) {
            try {
                sortingThread.join();
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
            }
        }
        visualizer.reset();
        visualizer.shuffle((ShuffleType) shuffleDropdown.getSelectedItem());
        sortingThread = unstartedSortingThread(visualizer);
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
