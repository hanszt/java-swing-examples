package hzt.search_algorithms;

import es.usc.citius.hipster.algorithm.Hipster;
import es.usc.citius.hipster.model.CostNode;
import es.usc.citius.hipster.model.Node;
import es.usc.citius.hipster.model.Transition;
import es.usc.citius.hipster.model.function.impl.StateTransitionFunction;
import es.usc.citius.hipster.model.impl.WeightedNode;
import es.usc.citius.hipster.model.problem.ProblemBuilder;
import es.usc.citius.hipster.model.problem.SearchProblem;
import es.usc.citius.hipster.util.examples.maze.Maze2D;
import es.usc.citius.hipster.util.examples.maze.Mazes;

import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Pablo Rodríguez Mier <<a href="mailto:pablo.rodriguez.mier@usc.es">pablo.rodriguez.mier@usc.es</a>>
 * Refactored by Hans Zuidervaart
 */
public class ASCIIMazeVisualizer {

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

    private final JFrame mainFrame;

    private Double evaluate(Transition<Void, Point> transition) {
        Point source = transition.getFromState();
        Point destination = transition.getState();
        double distance = source.distance(destination);
        return rounded(distance);
    }

    private void startAnimation(ActionEvent e) {
        switch (appState) {
            case STARTED:
                pause();
                break;
            case STOPPED:
                start();
                break;
            case PAUSED:
                continueExecution();
                break;
        }
    }

    private void stopAnimation(ActionEvent e) {
        stop();
        mazeTextArea.setText(asciiMaze());
    }

    private enum State {STOPPED, STARTED, PAUSED}

    // Global execution state
    private State appState = State.STOPPED;
    // Current algorithm used
    private Iterator<? extends Node<?, Point, ?>> algorithmIterator;
    // Move all to the algorithm executor
    private Set<Point> explored = new HashSet<>();
    private int steps = 0;
    private final Timer timer;
    private Maze2D maze;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }

        JFrame frame = new JFrame("Hipster Maze Shortest Path Visualizer V2");
        frame.setContentPane(new ASCIIMazeVisualizer(frame).mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public ASCIIMazeVisualizer(final JFrame frame) {
        this.mainFrame = frame;
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

        // Initialize the thread / listener for the execution
        // Listener to update the text area
        ExecutionHandler executionHandler = new ExecutionHandler();
        timer = new Timer((Integer) refreshSpinner.getValue(), executionHandler);
        refreshSpinner.addChangeListener(e -> timer.setDelay((Integer) refreshSpinner.getValue()));
        new Thread(executionHandler).start();
        timer.start();
    }

    private void loadSelectedMaze(JFrame frame) {
        mazeTextArea.setText(asciiMaze());
        // Resize to adapt the window
        frame.pack();
    }

    private void continueExecution() {
        if (appState.equals(State.PAUSED)) {
            runButton.setText("Pause");
            appState = State.STARTED;
        }
    }

    private void start() {
        // Create a new maze and run the selected algorithm
        steps = 0;
        try {
            maze = new Maze2D(mazeTextArea.getText().split("\\r?\\n"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame, ex.getMessage() + ". Try to reset the map.",
                    "Maze parse exception", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Create a new algorithm
        algorithmIterator = createAlgorithm(maze);
        // Reset explored tiles
        this.explored = new HashSet<>();
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

    private class ExecutionHandler implements ActionListener, Runnable {

        private ExecutionHandler() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (realtimePrintingCheckBox.isSelected() && appState.equals(State.STARTED)) {
                executeSearchStep();
            }
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (!realtimePrintingCheckBox.isSelected() && appState.equals(State.STARTED)) {
                        executeSearchStep();
                    } else {
                        //noinspection BusyWait
                        Thread.sleep(100);
                        Thread.yield();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private synchronized void executeSearchStep() {
            if (algorithmIterator.hasNext()) {
                Node<?, Point, ?> node = algorithmIterator.next();
                steps++;
                explored.add(node.state());
                if (realtimePrintingCheckBox.isSelected()) updateVisualizer(node, maze, explored);
                if (node.state().equals(maze.getGoalLoc())) {
                    updateVisualizer(node, maze, explored);
                    stop();
                }
            } else stop();
        }


        private synchronized void updateVisualizer(final Node<?, Point, ?> node, Maze2D maze, Collection<Point> explored) {
            if (node != null && maze != null) {
                List<Point> statePath = node.path().stream().map(Node::state).collect(Collectors.toList());

                String mazeStr = getMazeStringSolution(maze, explored, statePath);
                mazeTextArea.setText(mazeStr);
                // Update the status bar
                SwingUtilities.invokeLater(() -> printCost(node));
            }
        }

        private String getMazeStringSolution(Maze2D maze, Collection<Point> explored, Collection<Point> path) {
            List<Map<Point, Character>> replacements = new ArrayList<>();
            Map<Point, Character> replacement = new HashMap<>();
            for (Point p : explored) {
                replacement.put(p, '.');
            }
            replacements.add(replacement);
            replacement = new HashMap<>();
            for (Point p : path) {
                replacement.put(p, '*');
            }
            replacements.add(replacement);
            return maze.getReplacedMazeString(replacements);
        }

        @SuppressWarnings("rawtypes")
        private void printCost(Node<?, Point, ?> node) {
            labelSteps.setText(Integer.toString(steps));
            if (node instanceof CostNode) {
                CostNode n = (CostNode) node;
                labelCost.setText(new DecimalFormat("#.00").format(n.getCost()));
            }
        }

    }

    @SuppressWarnings("unchecked")
    private Iterator<? extends Node<?, Point, ?>> createAlgorithm(Maze2D maze) {
        switch (comboAlgorithm.getSelectedIndex()) {
            case 0:
                return Hipster.createDepthFirstSearch(buildProblem(maze, false)).iterator();
            case 1:
                return Hipster.createBreadthFirstSearch(buildProblem(maze, false)).iterator();
            case 2:
                return Hipster.createBellmanFord(buildProblem(maze, false)).iterator();
            case 3:
                return Hipster.createDijkstra(buildProblem(maze, false)).iterator();
            case 4:
                return Hipster.createAStar(buildProblem(maze, true)).iterator();
            case 5:
                return Hipster.createIDAStar(buildProblem(maze, true)).iterator();
            default:
                throw new IllegalStateException("Invalid algorithm");
        }
    }

    private static final String DELIMITER = String.format("%n");

    private String asciiMaze() {
        switch (comboMazes.getSelectedIndex()) {
            case 0:
                return String.join(DELIMITER, Mazes.exampleMaze1);
            case 1:
                return String.join(DELIMITER, Mazes.testMaze4);
            case 2:
                return String.join(DELIMITER, Mazes.testMaze3);
            case 3:
                return String.join(DELIMITER, Mazes.testMaze2);
            case 4:
                return String.join(DELIMITER, Mazes.testMaze5);
            default:
                throw new IllegalStateException("Unexpected value: " + comboMazes.getSelectedIndex());
        }
    }

    private SearchProblem<Void, Point, WeightedNode<Void, Point, Double>> buildProblem(
            final Maze2D maze, final boolean heuristic) {
        return ProblemBuilder.create()
                .initialState(maze.getInitialLoc())
                .defineProblemWithoutActions()
                .useTransitionFunction(getTransitionFunction(maze))
                .useCostFunction(this::evaluate)
                .useHeuristicFunction(state -> getHeuristic(maze, heuristic, state))
                .build();
    }

    private StateTransitionFunction<Point> getTransitionFunction(Maze2D maze) {
        return new StateTransitionFunction<>() {
            @Override
            public Iterable<Point> successorsOf(Point state) {
                return maze.validLocationsFrom(state);
            }
        };
    }

    private double getHeuristic(Maze2D maze, boolean heuristic, Point curLocation) {
        if (heuristic) {
            double distance = curLocation.distance(maze.getGoalLoc());
            return rounded(distance);
        } else return 0;
    }

    private double rounded(double distance) {
        return (double) Math.round(distance * 1e5) / 1e5;
    }
}
