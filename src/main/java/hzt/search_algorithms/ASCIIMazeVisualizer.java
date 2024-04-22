package hzt.search_algorithms;

import es.usc.citius.hipster.algorithm.Algorithm;
import es.usc.citius.hipster.model.CostNode;
import es.usc.citius.hipster.model.Node;
import es.usc.citius.hipster.model.Transition;
import es.usc.citius.hipster.model.impl.WeightedNode;
import es.usc.citius.hipster.model.problem.ProblemBuilder;
import es.usc.citius.hipster.model.problem.SearchProblem;
import es.usc.citius.hipster.util.examples.maze.Maze2D;
import es.usc.citius.hipster.util.examples.maze.Mazes;
import org.hzt.utils.It;
import org.hzt.utils.collections.ListX;
import org.hzt.utils.collections.MutableSetX;
import org.hzt.utils.collections.SetX;
import org.hzt.utils.sequences.Sequence;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import static es.usc.citius.hipster.algorithm.Hipster.createAStar;
import static es.usc.citius.hipster.algorithm.Hipster.createBellmanFord;
import static es.usc.citius.hipster.algorithm.Hipster.createBreadthFirstSearch;
import static es.usc.citius.hipster.algorithm.Hipster.createDepthFirstSearch;
import static es.usc.citius.hipster.algorithm.Hipster.createDijkstra;
import static es.usc.citius.hipster.algorithm.Hipster.createIDAStar;

/**
 * @author Pablo Rodríguez Mier <<a href="mailto:pablo.rodriguez.mier@usc.es">pablo.rodriguez.mier@usc.es</a>>
 * Refactored by Hans Zuidervaart
 */
public class ASCIIMazeVisualizer {

    private static final String DELIMITER = String.format("%n");
    private static final Logger LOGGER = LoggerFactory.getLogger(ASCIIMazeVisualizer.class);
    private static final Pattern SPLITTER = Pattern.compile("\\r?\\n");

    private JPanel mainPanel;
    private JComboBox<String> comboMazes;
    private JTextArea mazeTextArea;
    private JComboBox<String> comboAlgorithm;
    private JButton resetButton;
    private JButton runButton;
    private JSpinner refreshSpinner;
    private JCheckBox realtimePrintingCheckBox;
    private JLabel labelSteps;
    private JLabel labelCost;

    // Global execution state
    private final MutableSetX<Point> explored = MutableSetX.empty();
    private final Timer timer;

    private State appState = State.STOPPED;
    // Current algorithm used
    private Iterator<? extends Node<?, Point, ?>> algorithmIterator;
    // Move all to the algorithm executor
    private int steps = 0;
    private Maze2D maze;

    private final JFrame mainFrame;

    public ASCIIMazeVisualizer(final JFrame frame) {
        this.mainFrame = frame;
        timer = new Timer((int) refreshSpinner.getValue(), this::updateFrame);
        // Use double buffer for smooth updates
        mazeTextArea.setDoubleBuffered(true);
        refreshSpinner.setValue(50);

        // Default values
        comboMazes.addItem("Maze example 1");
        comboMazes.addItem("Maze example 2");
        comboMazes.addItem("Maze example 3");
        comboMazes.addItem("Maze example 4");
        comboMazes.addItem("Maze example 5");

        comboAlgorithm.addItem("Depth First Search (DFS, non-optimal)");
        comboAlgorithm.addItem("Breadth First Search (BFS, non-optimal) ");
        comboAlgorithm.addItem("Bellman Ford");
        comboAlgorithm.addItem("Dijkstra");
        comboAlgorithm.addItem("A*");
        comboAlgorithm.addItem("IDA*");
        mazeTextArea.setText(asciiMaze());

        runButton.addActionListener(this::startAnimation);
        resetButton.addActionListener(this::stopAnimation);
        comboMazes.addActionListener(e -> loadSelectedMaze(frame));
    }

    private void startAnimation(final ActionEvent e) {
        switch (appState) {
            case STARTED -> pause();
            case STOPPED -> start();
            case PAUSED -> continueExecution();
        }
    }

    private void stopAnimation(final ActionEvent e) {
        stop();
        mazeTextArea.setText(asciiMaze());
    }

    private enum State {STOPPED, STARTED, PAUSED}

    public static void main(final String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (final ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
            LOGGER.error("Error on startup: ", ex);
        }

        final var frame = new JFrame("Hipster Maze Shortest Path Visualizer V2");
        final var asciiMazeVisualizer = new ASCIIMazeVisualizer(frame);
        asciiMazeVisualizer.begin();
        frame.setContentPane(asciiMazeVisualizer.mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void begin() {
        // Initialize the thread / listener for the execution
        // Listener to update the text area
        refreshSpinner.addChangeListener(e -> timer.setDelay((int) refreshSpinner.getValue()));
        new Thread(this::run).start();
        timer.start();
    }

    private void loadSelectedMaze(final JFrame frame) {
        mazeTextArea.setText(asciiMaze());
        // Resize to adapt the window
        frame.pack();
    }

    private void continueExecution() {
        if (appState == State.PAUSED) {
            runButton.setText("Pause");
            appState = State.STARTED;
        }
    }

    private void start() {
        // Create a new maze and run the selected algorithm
        steps = 0;
        try {
            maze = new Maze2D(SPLITTER.split(mazeTextArea.getText()));
        } catch (final IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(mainFrame, ex.getMessage() + ". Try to reset the map.",
                    "Maze parse exception", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Create a new algorithm
        algorithmIterator = createAlgorithm(maze).iterator();
        // Reset explored tiles
        this.explored.clear();
        runButton.setText("Pause");
        appState = State.STARTED;
    }

    private void stop() {
        runButton.setText("Start");
        appState = State.STOPPED;
    }

    private void pause() {
        runButton.setText("Resume");
        appState = State.PAUSED;
    }

    private void updateFrame(final ActionEvent e) {
        if (realtimePrintingCheckBox.isSelected() && appState == State.STARTED) {
            executeSearchStep();
        }
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (!realtimePrintingCheckBox.isSelected() && appState == State.STARTED) {
                    executeSearchStep();
                } else {
                    //noinspection BusyWait
                    Thread.sleep(100);
                    Thread.yield();
                }
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private synchronized void executeSearchStep() {
        if (algorithmIterator.hasNext()) {
            final var node = algorithmIterator.next();
            steps++;
            explored.add(node.state());
            if (realtimePrintingCheckBox.isSelected()) {
                updateVisualizer(node, maze, explored);
            }
            if (node.state().equals(maze.getGoalLoc())) {
                updateVisualizer(node, maze, explored);
                stop();
            }
        } else {
            stop();
        }
    }


    private synchronized void updateVisualizer(@NotNull final Node<?, Point, ?> node,
                                               @NotNull final Maze2D maze,
                                               @NotNull final SetX<Point> explored) {
        final var statePath = Sequence.of(node.path())
                .map(Node::state)
                .toListX();

        final var mazeStr = getMazeStringSolution(maze, explored, statePath);
        mazeTextArea.setText(mazeStr);
        // Update the status bar
        SwingUtilities.invokeLater(() -> printCost(node));
    }

    private static String getMazeStringSolution(final Maze2D maze, final SetX<Point> explored, final ListX<Point> path) {
        return maze.getReplacedMazeString(List.of(
                explored.toMutableMap(It::self, point -> '.'),
                path.toMutableMap(It::self, point -> '*')));
    }

    private void printCost(final Node<?, Point, ?> node) {
        labelSteps.setText(Integer.toString(steps));
        if (node instanceof final CostNode<?, ?, ?, ?> costNode) {
            labelCost.setText(new DecimalFormat("#.00").format(costNode.getCost()));
        }
    }


    private Algorithm<Void, Point, WeightedNode<Void, Point, Double>> createAlgorithm(final Maze2D maze) {
        return switch (comboAlgorithm.getSelectedIndex()) {
            case 0 -> createDepthFirstSearch(buildSearchProblem(maze, false));
            case 1 -> createBreadthFirstSearch(buildSearchProblem(maze, false));
            case 2 -> createBellmanFord(buildSearchProblem(maze, false));
            case 3 -> createDijkstra(buildSearchProblem(maze, false));
            case 4 -> createAStar(buildSearchProblem(maze, true));
            case 5 -> createIDAStar(buildSearchProblem(maze, true));
            default -> throw new IllegalStateException("Invalid algorithm");
        };
    }

    private String asciiMaze() {
        final var selectedIndex = comboMazes.getSelectedIndex();
        return String.join(DELIMITER, switch (selectedIndex) {
            case 0 -> Mazes.exampleMaze1;
            case 1 -> Mazes.testMaze4;
            case 2 -> Mazes.testMaze3;
            case 3 -> Mazes.testMaze2;
            case 4 -> Mazes.testMaze5;
            default -> throw new IllegalStateException("Unexpected value: " + selectedIndex);
        });
    }

    private static SearchProblem<Void, Point, WeightedNode<Void, Point, Double>> buildSearchProblem(
            final Maze2D maze, final boolean heuristic) {
        return ProblemBuilder.create()
                .initialState(maze.getInitialLoc())
                .defineProblemWithoutActions()
                .useTransitionFunction(start -> createTransitions(maze, start))
                .useCostFunction(ASCIIMazeVisualizer::calculateDistance)
                .useHeuristicFunction(current -> heuristic ? rounded(current.distance(maze.getGoalLoc())) : 0)
                .build();
    }

    private static Sequence<Transition<Void, Point>> createTransitions(final Maze2D maze, final Point start) {
        return Sequence.of(maze.validLocationsFrom(start))
                .map(current -> Transition.create(start, current));
    }

    private static double calculateDistance(final Transition<Void, Point> transition) {
        final var source = transition.getFromState();
        final var destination = transition.getState();
        final var distance = source.distance(destination);
        return rounded(distance);
    }

    private static double rounded(final double distance) {
        return Math.round(distance * 1e5) / 1e5;
    }
}
