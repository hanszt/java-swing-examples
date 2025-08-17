package org.hzt.ant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * The Board class represents a graphical simulation environment for managing a colony of ants.
 * It extends javax.swing.JPanel and implements java.awt.event.MouseListener to support graphical rendering
 * and user interaction via mouse events. The class handles the initialization, management, and simulation
 * of game entities, including tiles, ants, and environmental elements, using various methods.
 * <p>
 * Fields:
 * - LOGGER: Logger for debugging and tracking runtime status.
 * - serialVersionUID: Unique identifier for serialization compatibility.
 * - timer: Timer controlling the simulation updates.
 * - DELAY: Determines the interval between simulation updates.
 * - start: Boolean flag to indicate simulation start.
 * - running: Boolean flag to indicate if the simulation is running.
 * - WIDTH_TILES, HEIGHT_TILES, TOTAL_TILES, SIZE_TILES, BOARD_WIDTH: Dimensions and size configurations for the board.
 * - foundFood, foundHome, foundWater, foundPoison: Flags or counters for environmental element discoveries.
 * - foodSearch, homeSearch, waterSearch: State indicators for different search objectives.
 * - states, graph: Data structures holding game states and board graph topology.
 * - ants: List of ant objects representing the colony.
 * - antHillAdded: Tracks whether an anthill has been added to the board.
 * - antFSM: Finite state machine dictating ant behavior.
 * - nodes, icons, occupants, terrain: Collections managing visual and functional aspects of the board.
 * - ant, antHill, antWithFood, antInWater, antInHill: Entities representing various states or types of ants.
 * - buttonPane: Component for housing GUI controls.
 * - execute: Used to trigger computational tasks like pathfinding.
 * - blank: Used to reset board settings to an empty state.
 * - random: Random generator for procedural generation.
 */
public final class Board extends JPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(Board.class);

    @Serial
    private static final long serialVersionUID = 1L;
    private static final int DELAY = 50;
    private boolean running = false;

    private static final int WIDTH_TILES = 30;
    private static final int HEIGHT_TILES = 30;
    private static final int TOTAL_TILES = WIDTH_TILES * HEIGHT_TILES;
    private static final int SIZE_TILES = 25;
    private static final int BOARD_WIDTH = WIDTH_TILES * SIZE_TILES;

    private Graph graph = new Graph();

    private final List<Ant> ants = new ArrayList<>();
    private boolean antHillAdded = false;
    private Map<Ant, FiniteStateMachine> antFSM = new HashMap<>();
    private final Node[] nodes = new Node[TOTAL_TILES];
    private final List<String> occupants = List.of("food", "water", "terrain", "poison");
    private final Image terrain = getImageIcon("/images/terrain.png").getImage();
    private final Image ant = getImageIcon("/images/ant.png").getImage();
    private final Image antHill = getImageIcon("/images/antHill.png").getImage();
    private final Image antWithFood = getImageIcon("/images/antWithFood.png").getImage();
    private final Image antInWater = getImageIcon("/images/antInWater.png").getImage();
    private final Image antInHill = getImageIcon("/images/antInHill.png").getImage();

    private final JButton startButton = new JButton("Start");

    private final RandomGenerator random;

    public Board(final RandomGenerator random) {
        this.random = random;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                final var position = e.getPoint();
                if (position.x < BOARD_WIDTH && position.y < BOARD_WIDTH && !running) {
                    final var x = (int) Math.floor(position.x / SIZE_TILES);
                    final var y = (int) Math.floor(position.y / SIZE_TILES);
                    final var point = new Point(position.x - position.x % SIZE_TILES, position.y - position.y % SIZE_TILES);
                    if (!nodes[x * WIDTH_TILES + y].isOccupied() && !antHillAdded) {
                        addAnt(x * WIDTH_TILES + y, x * WIDTH_TILES + y);
                        final var occupied = true;
                        nodes[x * WIDTH_TILES + y] = new Node(
                                point,
                                getImageIcon("/images/" + "antHill.png").getImage(),
                                occupied,
                                "antHill"
                        );
                        antHillAdded = true;
                    } else if (nodes[x * WIDTH_TILES + y].isOccupied()) {
                        addAnt(x * WIDTH_TILES + y, x * WIDTH_TILES + y);
                    }
                }
            }
        });
        setFocusable(true);
        setPreferredSize(new Dimension(BOARD_WIDTH, 770));
        setBackground(Color.WHITE);
        setDoubleBuffered(true);
        setLayout(new BorderLayout());

        startButton.addActionListener(e -> start());

        final JButton blankButton = new JButton("Blank");
        blankButton.addActionListener(e -> makeBlank());

        final var buttonPane = new JPanel(new GridLayout(1, 3));
        buttonPane.setLayout(new GridLayout(1, 2));
        buttonPane.add(blankButton);
        buttonPane.add(startButton);
        add(buttonPane, BorderLayout.SOUTH);

        for (var i = 0; i < nodes.length; i++) {
            nodes[i] = new Node();
        }

        blankButton.doClick();


        final var timer = new Timer(DELAY, e -> repaint());
        timer.start();
    }

    private static ImageIcon getImageIcon(final String name) {
        final var resource = Optional.ofNullable(Board.class.getResource(name))
                .map(URL::getFile)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + name));
        return new ImageIcon(resource);
    }

    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
        Toolkit.getDefaultToolkit().sync();
    }

    private void doDrawing(final Graphics g) {
        final var g2d = (Graphics2D) g;
        for (var i = 0; i < WIDTH_TILES; i++) {
            for (var j = 0; j < HEIGHT_TILES; j++) {
                final var image = nodes[i * WIDTH_TILES + j].getImage();
                final var x = nodes[i * WIDTH_TILES + j].getTile().x;
                final var y = nodes[i * HEIGHT_TILES + j].getTile().y;
                g2d.drawImage(image, x, y, this);
            }
        }
        if (running) {
            moveAnts();
        }
    }


    public synchronized void moveAnts() {
        final var deadAnts = new ArrayList<Ant>();
        final var bornAnts = new ArrayList<Integer>();
        for (var i = 0; i < ants.size(); i++) {
            var deadAnt = 999999;// determines whether an ant has died and holds the index
            var antBorn = 999999;// determines whether an ant has been born and holds the index

            final var iAnt = ants.get(i);

            if (iAnt.getActions().isEmpty()) {
                iAnt.setActions(antFSM.get(iAnt).update());
            }

            final var action = iAnt.getActions().removeFirst();

            var newPosition = iAnt.getCurrent();
            var alive = true;
            antFSM.get(iAnt).getCurrentState().firstTransitionOrThrow().setActive(false);// sets the found food trigger to false

            switch (action) {
                case Action.DIE -> {
                    nodes[iAnt.getCurrent()].setImage(terrain);
                    nodes[iAnt.getCurrent()].setOccupant("terrain");
                    alive = false;
                }
                case Action.SEARCH_FOR_FOOD -> {
                    newPosition = wander(iAnt);
                    nodes[iAnt.getCurrent()].setImage(getImageIcon("/images/" + nodes[iAnt.getCurrent()].getOccupant() + ".png").getImage());
                    iAnt.setCurrent(newPosition);
                    nodes[iAnt.getCurrent()].setImage(ant);
                    if (nodes[newPosition].getOccupant().equals("food")) {
                        antFSM.get(iAnt).getCurrentState().firstTransitionOrThrow().setActive(true);
                    }
                }
                case Action.PICK_UP_FOOD -> {
                    nodes[iAnt.getCurrent()].setImage(antWithFood);
                    nodes[iAnt.getCurrent()].setOccupant("terrain");
                }
                case Action.ENTER_ANT_HILL -> nodes[iAnt.getCurrent()].setImage(antInHill);
                case Action.IN_ANT_HILL -> nodes[iAnt.getCurrent()].setImage(antHill);
                case Action.LEAVE_ANT_HILL -> nodes[iAnt.getCurrent()].setImage(antInHill);
                case Action.SEARCH_FOR_HOME -> {
                    newPosition = getAStar(iAnt);
                    nodes[iAnt.getCurrent()].setImage(getImageIcon("/images/" + nodes[iAnt.getCurrent()].getOccupant() + ".png").getImage());
                    iAnt.setCurrent(newPosition);
                    nodes[iAnt.getCurrent()].setImage(antWithFood);
                    if (iAnt.getHome() == newPosition) {
                        antBorn = iAnt.getHome();
                        antFSM.get(iAnt).getCurrentState().firstTransitionOrThrow().setActive(true);
                        nodes[iAnt.getCurrent()].setImage(antInHill);
                    }
                }
                case Action.SEARCH_FOR_WATER -> {
                    newPosition = wander(iAnt);
                    nodes[iAnt.getCurrent()].setImage(getImageIcon("/images/" + nodes[iAnt.getCurrent()].getOccupant() + ".png").getImage());
                    iAnt.setCurrent(newPosition);
                    nodes[iAnt.getCurrent()].setImage(ant);
                    if (nodes[newPosition].getOccupant().equals("water")) {
                        antFSM.get(iAnt).getCurrentState().firstTransitionOrThrow().setActive(true);
                    }
                }
                case Action.DRINK_WATER -> {
                    nodes[iAnt.getCurrent()].setImage(antInWater);
                    nodes[iAnt.getCurrent()].setOccupant("terrain");
                }
            }

            if (nodes[newPosition].getOccupant().equals("poison")) {
                nodes[iAnt.getCurrent()].setImage(terrain);
                nodes[iAnt.getCurrent()].setOccupant("terrain");
                alive = false;
                deadAnt = i;
            }

            if (alive) {
                final var ant = ants.get(i);
                if (ant.getCurrent() == ant.getHome() &&
                    !action.equals(Action.ENTER_ANT_HILL) &&
                    !action.equals(Action.LEAVE_ANT_HILL) &&
                    !action.equals(Action.SEARCH_FOR_HOME)) {
                    nodes[ant.getCurrent()].setImage(getImageIcon("/images/" + "antHill.png").getImage());
                }
            }

            if (deadAnt != 999999) {
                deadAnts.add(iAnt);
            }

            if (antBorn != 999999) {
                bornAnts.add(antBorn);
            }
        }

        for (final var ant : deadAnts) {
            ants.remove(ant);
        }

        for (final var node : bornAnts) {
            addAnt(node, node);
        }
    }

    public int getAStar(final Ant ant) {
        final var path = AStar.pathFindAStar(graph, ant.getCurrent(), ant.getHome(), new Heuristic());
        final List<Integer> nodesTo = new ArrayList<>();
        for (final var connection : path) {
            nodesTo.add(connection.toNode());
        }
        return nodesTo.removeFirst();
    }

    public int wander(final Ant iAnt) {
        final var num = random.nextInt(graph.getConnections(iAnt.getCurrent()).size());
        return graph.getConnections(iAnt.getCurrent()).get(num).toNode();
    }

    private void makeBlank() {
        LOGGER.info("Make blank");
        ants.clear();
        antFSM = new HashMap<>();
        antHillAdded = false;
        blank();
        startButton.setEnabled(true);
        repaint();
    }

    private void start() {
        LOGGER.info("Start");
        if (antHillAdded) {
            randomize();
            startSim();
            startButton.setEnabled(false);
        }
        repaint();
    }

    public void blank() {
        running = false;
        for (var i = 0; i < TOTAL_TILES; i++) {
            final var occupied = false;
            nodes[i] = new Node(
                    new Point(i / WIDTH_TILES * SIZE_TILES, i % HEIGHT_TILES * SIZE_TILES),
                    getImageIcon("/images/" + "empty.png").getImage(),
                    occupied,
                    "empty"
            );
        }
    }

    /**
     * fills tiles with random entities
     */
    public void randomize() {
        for (var i = 0; i < WIDTH_TILES; i++) {
            for (var j = 0; j < HEIGHT_TILES; j++) {
                final var r = random.nextInt(32);
                final int num;
                if (r < 4) {
                    num = 0;
                } else if (r < 8) {
                    num = 1;
                } else if (r < 31) {
                    num = 2;
                } else {
                    num = 3;
                }
                if (!nodes[i * WIDTH_TILES + j].isOccupied()) {
                    final var occupied = true;
                    nodes[i * WIDTH_TILES + j] = new Node(
                            new Point(i * BOARD_WIDTH / WIDTH_TILES, j * BOARD_WIDTH / HEIGHT_TILES),
                            getImageIcon("/images/" + occupants.get(num) + ".png").getImage(),
                            occupied,
                            occupants.get(num)
                    );
                }
            }
        }
    }

    public void startSim() {
        graph = new Graph();
        for (var i = 0; i < WIDTH_TILES; i++) {
            for (var j = 0; j < HEIGHT_TILES; j++) {
                addNeighbours(i, j);
            }
        }
        running = true;
    }

    public void addNeighbours(final int i, final int j) {
        final int fromNode;
        int toNode;
        final List<Connection> connections = new ArrayList<>();
        fromNode = i * WIDTH_TILES + j;
        if (i > 0 && j > 0) {
            toNode = (i - 1) * WIDTH_TILES + (j - 1);
            connections.add(new Connection(fromNode, toNode, 0));
        }
        if (i > 0) {
            toNode = (i - 1) * WIDTH_TILES + (j);
            connections.add(new Connection(fromNode, toNode, 0));
        }
        if (i > 0 && j < (WIDTH_TILES - 1)) {
            toNode = (i - 1) * WIDTH_TILES + (j + 1);
            connections.add(new Connection(fromNode, toNode, 0));
        }
        if (j > 0) {
            toNode = (i) * WIDTH_TILES + (j - 1);
            connections.add(new Connection(fromNode, toNode, 0));
        }
        if (j < (WIDTH_TILES - 1)) {
            toNode = (i) * WIDTH_TILES + (j + 1);
            connections.add(new Connection(fromNode, toNode, 0));
        }
        if (i < (WIDTH_TILES - 1) && j > 0) {
            toNode = (i + 1) * WIDTH_TILES + (j - 1);
            connections.add(new Connection(fromNode, toNode, 0));
        }
        if (i < (WIDTH_TILES - 1)) {
            toNode = (i + 1) * WIDTH_TILES + (j);
            connections.add(new Connection(fromNode, toNode, 0));
        }
        if (i < (WIDTH_TILES - 1) && j < (WIDTH_TILES - 1)) {
            toNode = (i + 1) * WIDTH_TILES + (j + 1);
            connections.add(new Connection(fromNode, toNode, 0));
        }
        graph.addConnections(new ArrayList<>(connections));
    }


    public void addAnt(final int x, final int y) {
        final Transition foundFood = new Transition(Action.PICK_UP_FOOD, false);
        final Transition foundHome = new Transition(Action.IN_ANT_HILL, false);
        final Transition foundWater = new Transition(Action.DRINK_WATER, false);

        final var foundPoison = new Transition(Action.DIE, false);

        final var foodSearch = new State(Action.SEARCH_FOR_FOOD, Action.DRINK_WATER, Action.PICK_UP_FOOD, List.of(foundFood, foundPoison));

        final var homeSearch = new State(Action.SEARCH_FOR_HOME, Action.PICK_UP_FOOD, Action.ENTER_ANT_HILL, List.of(foundHome, foundPoison));


        final var waterSearch = new State(Action.SEARCH_FOR_WATER, Action.LEAVE_ANT_HILL, Action.DRINK_WATER, List.of(foundWater, foundPoison));


        foundFood.setTargetState(homeSearch);
        foundHome.setTargetState(waterSearch);
        foundWater.setTargetState(foodSearch);
        foundPoison.setTargetState(null);


        final List<State> states = new ArrayList<>();
        states.add(foodSearch);
        states.add(homeSearch);
        states.add(waterSearch);

        final var fsm = new FiniteStateMachine(states, foodSearch);
        final var ant = new Ant(x, y);
        ants.add(ant);
        antFSM.put(ant, fsm);
    }

    Node[] getNodes() {
        return Arrays.copyOf(nodes, nodes.length);
    }
}
