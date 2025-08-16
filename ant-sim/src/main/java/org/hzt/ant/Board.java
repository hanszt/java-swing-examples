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
    private static final long serialVersionUID = 1L;// serialization variable
    private static final int DELAY = 50;// used to set the between-event delay
    private boolean running = false;// represents when the game is running

    private static final int WIDTH_TILES = 30;// the number of width tiles on the game board
    private static final int HEIGHT_TILES = 30;// the number of height tiles on the game board
    private static final int TOTAL_TILES = WIDTH_TILES * HEIGHT_TILES;// the total number of tiles on the game board
    private static final int SIZE_TILES = 25;// the total number of tiles on the game board
    private static final int BOARD_WIDTH = WIDTH_TILES * SIZE_TILES;// the total number of tiles on the game board

    private Graph graph = new Graph();// the graph used to represent the org.hzt.ant.Game space

    private final List<Ant> ants = new ArrayList<>();// holds all the ants in the colony
    private boolean antHillAdded = false;// represents whether or not the ant hill has been placed
    //private List<org.hzt.ant.FiniteStateMachine> fsms = new ArrayList<org.hzt.ant.FiniteStateMachine>();// the FSMs used - one for each ant
    private Map<Ant, FiniteStateMachine> antFSM = new HashMap<>(); // holds each set of org.hzt.ant.Ant/FSM combinations
    private final Node[] nodes = new Node[TOTAL_TILES];// used to represent each node/tile in the org.hzt.ant.Game space
    private final String[] icons = {"food.png", "water.png", "terrain.png", "poison.png"};// the different images used for the tiles
    private final String[] occupants = {"food", "water", "terrain", "poison"};// the different objects used in the game
    private final Image terrain;
    private final Image ant;
    private final Image antHill;
    private final Image antWithFood;
    private final Image antInWater;
    private final Image antInHill;// images used in the game

    private final JButton startButton = new JButton("Start");// the button to execute the ant colony

    private final RandomGenerator random;

    public Board(final RandomGenerator random) {
        this.random = random;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                final var position = e.getPoint();// gets coordinates of the click
                if (position.x < BOARD_WIDTH && position.y < BOARD_WIDTH && !running) {// determines if the click is within the bounds of the game space
                    final var x = (int) Math.floor(position.x / SIZE_TILES);// gets the x tile value
                    final var y = (int) Math.floor(position.y / SIZE_TILES);// gets the y tile value
                    final var point = new Point(position.x - position.x % SIZE_TILES, position.y - position.y % SIZE_TILES);// gets the uper left coordinate of the tile
                    if (!nodes[x * WIDTH_TILES + y].isOccupied() && !antHillAdded) {// checks if an ant needs to be added
                        addAnt(x * WIDTH_TILES + y, x * WIDTH_TILES + y);// calls the add ant method
                        nodes[x * WIDTH_TILES + y] = new Node(point, getImageIcon("/images/" + "antHill.png").getImage(), true, "antHill");// adds the antHill at specified node
                        antHillAdded = true;// marks anthill flag as created
                    } else if (nodes[x * WIDTH_TILES + y].isOccupied()) {// allows additional ants to be added to the colony
                        addAnt(x * WIDTH_TILES + y, x * WIDTH_TILES + y);// calls addAnt method
                    }
                }
            }
        });// adds a Mouse listener on the game space
        setFocusable(true);// allows focusable
        setPreferredSize(new Dimension(BOARD_WIDTH, 770));// sets the preferred size of the game space window
        setBackground(Color.WHITE);// sets the background color of the display to white
        setDoubleBuffered(true);// sets double buffered to true
        setLayout(new BorderLayout());// creates the layout for the game panel

        startButton.addActionListener(e -> start());// adds a listener for the execute button
        // the button to start over
        final JButton blank = new JButton("Blank");
        blank.addActionListener(e -> makeBlank());// adds a listener for the blank button
        // adds a JPanel to hold the buttons
        final JPanel buttonPane = new JPanel(new GridLayout(1, 3));
        buttonPane.setLayout(new GridLayout(1, 2));// sets the layout for the buttonPane JPanel pane
        buttonPane.add(blank);// adds the fullBoard button to the buttonPane
        buttonPane.add(startButton);// adds the execute button to the buttonPane
        add(buttonPane, BorderLayout.SOUTH);// adds the buttonPane to the JPanel displaying the game

        for (var i = 0; i < nodes.length; i++)// loops through the game nodes
            nodes[i] = new Node();// fills each element with a blank node to start

        terrain = getImageIcon("/images/terrain.png").getImage();// creates the terrain image
        ant = getImageIcon("/images/ant.png").getImage();// creates the ant image
        antHill = getImageIcon("/images/antHill.png").getImage();// creates the antHill image
        antWithFood = getImageIcon("/images/antWithFood.png").getImage();// creates the antWithFood image
        antInWater = getImageIcon("/images/antInWater.png").getImage();// creates the antInWater image
        antInHill = getImageIcon("/images/antInHill.png").getImage();// creates the antInHill image

        blank.doClick();// calls the method to produce a blank board to start

        // represents the game timer
        final var timer = new Timer(DELAY, e -> repaint());// creates timer with between-event DELAY
        timer.start();// starts the timer
    }// org.hzt.ant.Board() constructor

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
                g2d.drawImage(image, x, y, this);// draws each tile with its current information
            }
        }
        if (running) {
            moveAnts();
        }
    }

    // moveAnts method - governs the movement of the ants, with the FSM
    public synchronized void moveAnts() {
        final var deadAnts = new ArrayList<Ant>();// holds all ants killed during current loop
        final var bornAnts = new ArrayList<Integer>();// holds all ants
        for (var i = 0; i < ants.size(); i++) {// loops through all the ants
            var deadAnt = 999999;// determines whether an ant has died and holds the index
            var antBorn = 999999;// determines whether an ant has been born and holds the index

            final var iAnt = ants.get(i);// creates duplicate of each Spider, necessary for deletion purposes

            if (iAnt.getActions().isEmpty())// checks if the org.hzt.ant.Ant has any current actions to complete
                iAnt.setActions(antFSM.get(iAnt).update());// if not, gets actions from FSM

            final var action = iAnt.getActions().removeFirst();// gets the first action in the list

            var newPosition = iAnt.getCurrent();// will hold the next position of the ant
            var alive = true;// used to flag ant deaths
            antFSM.get(iAnt).getCurrentState().firstTransitionOrThrow().setActive(false);// sets the found food trigger to false
            antFSM.get(iAnt).getCurrentState().firstTransitionOrThrow().setActive(false);// sets the found food trigger to false
            antFSM.get(iAnt).getCurrentState().firstTransitionOrThrow().setActive(false);// sets the found food trigger to false

            switch (action) {
                case Action.DIE -> {
                    nodes[iAnt.getCurrent()].setImage(terrain);
                    nodes[iAnt.getCurrent()].setOccupant("terrain");
                    alive = false;
                }
                case Action.SEARCH_FOR_FOOD -> {
                    newPosition = wander(iAnt);// gets new position through wander method
                    nodes[iAnt.getCurrent()].setImage(getImageIcon("/images/" + nodes[iAnt.getCurrent()].getOccupant() + ".png").getImage());// sets current node image as previous occupant
                    iAnt.setCurrent(newPosition);// updates the ant position
                    nodes[iAnt.getCurrent()].setImage(ant);// sets the new node image as the ant
                    if (nodes[newPosition].getOccupant().equals("food")) {// checks if food has been found
                        antFSM.get(iAnt).getCurrentState().firstTransitionOrThrow().setActive(true);// updates foundFood trigger
                    }
                }
                case Action.PICK_UP_FOOD -> {
                    nodes[iAnt.getCurrent()].setImage(antWithFood);// sets node image to ant with food
                    nodes[iAnt.getCurrent()].setOccupant("terrain");// sets node occupant to terrain
                }
                case Action.ENTER_ANT_HILL -> nodes[iAnt.getCurrent()].setImage(antInHill);
                case Action.IN_ANT_HILL -> nodes[iAnt.getCurrent()].setImage(antHill);// updates node image to ant hill
                case Action.LEAVE_ANT_HILL ->
                        nodes[iAnt.getCurrent()].setImage(antInHill);// updates node image to ant in hill
                case Action.SEARCH_FOR_HOME -> {
                    newPosition = getAStar(iAnt);// gets new position from A*
                    nodes[iAnt.getCurrent()].setImage(getImageIcon("/images/" + nodes[iAnt.getCurrent()].getOccupant() + ".png").getImage());// updates old node image to current occupant
                    iAnt.setCurrent(newPosition);// updates ant position
                    nodes[iAnt.getCurrent()].setImage(antWithFood);// updates new node image to ant with food
                    if (iAnt.getHome() == newPosition) {// if home has been found
                        antBorn = iAnt.getHome();// gets node for new ant
                        antFSM.get(iAnt).getCurrentState().firstTransitionOrThrow().setActive(true);// sets found home trigger to true
                        nodes[iAnt.getCurrent()].setImage(antInHill);// sets new node image to ant in hill
                    }
                }
                case Action.SEARCH_FOR_WATER -> {
                    newPosition = wander(iAnt);// gets new position from wander
                    nodes[iAnt.getCurrent()].setImage(getImageIcon("/images/" + nodes[iAnt.getCurrent()].getOccupant() + ".png").getImage());// updates old node to previous occupant image
                    iAnt.setCurrent(newPosition);// updates ant position
                    nodes[iAnt.getCurrent()].setImage(ant);// updates new node image to ant
                    if (nodes[newPosition].getOccupant().equals("water")) {// checks if water has been found
                        antFSM.get(iAnt).getCurrentState().firstTransitionOrThrow().setActive(true);// sets found water trigger to true
                    }
                }
                case Action.DRINK_WATER -> {
                    nodes[iAnt.getCurrent()].setImage(antInWater);// updates node image to ant in water
                    nodes[iAnt.getCurrent()].setOccupant("terrain");// updates node occupant to terrain
                }
            }

            if (nodes[newPosition].getOccupant().equals("poison")) {// checks if ant steps in poison
                nodes[iAnt.getCurrent()].setImage(terrain);// updates the node image to terrain
                nodes[iAnt.getCurrent()].setOccupant("terrain");// updates the node occupant to terrain
                alive = false;// flags that ant was killed
                deadAnt = i;// gets number of ant killed
            }

            if (alive) {
                final var ant = ants.get(i);
                if (ant.getCurrent() == ant.getHome() &&
                    !action.equals(Action.ENTER_ANT_HILL) &&
                    !action.equals(Action.LEAVE_ANT_HILL) &&
                    !action.equals(Action.SEARCH_FOR_HOME))
                    nodes[ant.getCurrent()].setImage(getImageIcon("/images/" + "antHill.png").getImage());
            }

            if (deadAnt != 999999) {
                deadAnts.add(iAnt);
            }

            if (antBorn != 999999) {// check if an ant was born
                bornAnts.add(antBorn);// adds number of ant born
            }
        }

        for (final var ant : deadAnts)// loops through all ants killed in current game loop
            ants.remove(ant);// removes ants killed during game loop

        for (final var node : bornAnts)// loops through all ant born in current game loop
            addAnt(node, node);// adds ants born during game loop
    }// moveAnts() method

    // getAStar method - calls the A* algorithm to help the org.hzt.ant.Ant find its way home
    public int getAStar(final Ant ant) {
        final var path = AStar.pathFindAStar(graph, ant.getCurrent(), ant.getHome(), new Heuristic());// creates a new path from the A* result
        final List<Integer> nodesTo = new ArrayList<>();// list to hold the toNodes on the path
        for (final var connection : path)// loops through the path
            nodesTo.add(connection.toNode());// adds the toNodes to the list
        return nodesTo.removeFirst();// returns the first node
    }// getAStar(org.hzt.ant.Ant) method

    // wander method - moves Ant randomly, even odds between all current connections
    public int wander(final Ant iAnt) {
        final var num = random.nextInt(graph.getConnections(iAnt.getCurrent()).size());// gets a random int between 0 and num of connections currently
        return graph.getConnections(iAnt.getCurrent()).get(num).toNode();// returns the random connection to move ant
    }// wander(org.hzt.ant.Ant) method

    private void makeBlank() {
        LOGGER.info("Make blank");
        ants.clear();// resets the list of ants
        antFSM = new HashMap<>();// resets the fsms
        antHillAdded = false;// sets the ant hill added to false
        blank();// calls the method to empty the board
        startButton.setEnabled(true);// enables the execute button
        repaint();
    }

    private void start() {
        LOGGER.info("Start");
        if (antHillAdded) {// makes sure an ant hill has been added
            randomize();// calls the randomize method to fill the board with random tiles
            startSim();// calls the method to execute the search
            startButton.setEnabled(false);// disables execute button
        }
        repaint();
    }

    public void blank() {
        running = false;// sets the simulation to stopped
        for (var i = 0; i < TOTAL_TILES; i++)// loops through all tiles
            nodes[i] = new Node(new Point(i / WIDTH_TILES * SIZE_TILES, i % HEIGHT_TILES * SIZE_TILES), getImageIcon("/images/" + "empty.png").getImage(), false, "empty");// sets empty tile
    }

    /**
     * fills tiles with random entities
     */
    public void randomize() {
        int r;// will hold the random int
        int num;// will hold the filtered random int that determines with entity to use for a tile
        for (var i = 0; i < WIDTH_TILES; i++) {// loops x-coords
            for (var j = 0; j < HEIGHT_TILES; j++) {// loops y-coords
                r = random.nextInt(32);// gets random int from 0 to 32
                if (r < 4) num = 0;
                else if (r < 8) num = 1;
                else if (r < 31) num = 2;
                else num = 3;// distributes different objects
                if (!nodes[i * WIDTH_TILES + j].isOccupied()) {// if tile empty or random chosen
                    nodes[i * WIDTH_TILES + j] = new Node(new Point(i * BOARD_WIDTH / WIDTH_TILES, j * BOARD_WIDTH / HEIGHT_TILES), getImageIcon("/images/" + icons[num]).getImage(), true, occupants[num]);// creates random tile
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
        final int fromNode;// origin node of the connection
        int toNode;// destination node of the connection
        final List<Connection> connections = new ArrayList<>();// will hold the connections
        fromNode = i * WIDTH_TILES + j;// calculates the from node by the coordinates
        if (i > 0 && j > 0) {// not on left or top game border
            toNode = (i - 1) * WIDTH_TILES + (j - 1);// calculates to node of connection from coords
            connections.add(new Connection(fromNode, toNode, 0));// add new connection
        }
        if (i > 0) {// not on left game border
            toNode = (i - 1) * WIDTH_TILES + (j);// calculates to node of connection from coords
            connections.add(new Connection(fromNode, toNode, 0));// add new connection
        }
        if (i > 0 && j < (WIDTH_TILES - 1)) {// not on left or bottom game border
            toNode = (i - 1) * WIDTH_TILES + (j + 1);// calculates to node of connection from coords
            connections.add(new Connection(fromNode, toNode, 0));// add new connection
        }
        if (j > 0) {// not on top game border
            toNode = (i) * WIDTH_TILES + (j - 1);// calculates to node of connection from coords
            connections.add(new Connection(fromNode, toNode, 0));// add new connection
        }
        if (j < (WIDTH_TILES - 1)) {// not on bottom game border
            toNode = (i) * WIDTH_TILES + (j + 1);// calculates to node of connection from coords
            connections.add(new Connection(fromNode, toNode, 0));// add new connection
        }
        if (i < (WIDTH_TILES - 1) && j > 0) {// not on right or top game border
            toNode = (i + 1) * WIDTH_TILES + (j - 1);// calculates to node of connection from coords
            connections.add(new Connection(fromNode, toNode, 0));// add new connection
        }
        if (i < (WIDTH_TILES - 1)) {// not on right game border
            toNode = (i + 1) * WIDTH_TILES + (j);// calculates to node of connection from coords
            connections.add(new Connection(fromNode, toNode, 0));// add new connection
        }
        if (i < (WIDTH_TILES - 1) && j < (WIDTH_TILES - 1)) {// not on right or bottom game border
            toNode = (i + 1) * WIDTH_TILES + (j + 1);// calculates to node of connection from coords
            connections.add(new Connection(fromNode, toNode, 0));// add new connection
        }
        graph.addConnections(new ArrayList<>(connections));// adds the connections for the current node to the graph
    }

    // addAnt method - takes care of the details involved with adding ants to the colony
    public void addAnt(final int x, final int y) {
        final Transition foundFood = new Transition(Action.PICK_UP_FOOD, false);// initializes the foundFood transition
        final Transition foundHome = new Transition(Action.IN_ANT_HILL, false);// initializes the foundHome transition
        final Transition foundWater = new Transition(Action.DRINK_WATER, false);// initializes the foundWater transition
        // transitions used with the FSM
        final var foundPoison = new Transition(Action.DIE, false);// initializes the foundPoison transition

        final var foodSearch = new State(Action.SEARCH_FOR_FOOD, Action.DRINK_WATER, Action.PICK_UP_FOOD, List.of(foundFood, foundPoison));
        // initializes the foodSearch state
        final var homeSearch = new State(Action.SEARCH_FOR_HOME, Action.PICK_UP_FOOD, Action.ENTER_ANT_HILL, List.of(foundHome, foundPoison));
        // initializes the homeSearch state
        // states used with the FSM
        final var waterSearch = new State(Action.SEARCH_FOR_WATER, Action.LEAVE_ANT_HILL, Action.DRINK_WATER, List.of(foundWater, foundPoison));
        // initializes the waterSearch state

        foundFood.setTargetState(homeSearch);// adds the homeSearch as the target state for foundFood
        foundHome.setTargetState(waterSearch);// adds the waterSearch as the target state for foundHome
        foundWater.setTargetState(foodSearch);// adds the foodSearch as the target state for foundWater
        foundPoison.setTargetState(null);// adds the homeSearch as the target state for foundPoison

        // holds all the states used with the FSM
        final List<State> states = new ArrayList<>();// initializes the states collection
        states.add(foodSearch);// adds the foodSearch state
        states.add(homeSearch);// adds the homeSearch state
        states.add(waterSearch);// adds the waterSearch state

        final var fsm = new FiniteStateMachine(states, foodSearch);// creates a new FSM to pair with the ant
        final var ant = new Ant(x, y);// creates a new instance of the org.hzt.ant.Ant
        ants.add(ant);// home, current
        antFSM.put(ant, fsm);// creates an ant/fsm pair
    }
}
