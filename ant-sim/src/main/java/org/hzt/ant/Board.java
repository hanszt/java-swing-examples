package org.hzt.ant;/*
 * org.hzt.ant.Board class - handles the bulk of the game duties.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public final class Board extends JPanel implements MouseListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(Board.class);

    private static final long serialVersionUID = 1L;// serialization variable
    private final Timer timer;// represents the game timer
    private static final int DELAY = 10;// used to set the between-event delay
    private int start = 0;// used to represent the start node in the search, where the ant is
    private boolean running = false;// represents when the game is running

    private static final int WIDTH_TILES = 30;// the number of width tiles on the game board
    private static final int HEIGHT_TILES = 30;// the number of height tiles on the game board
    private static final int TOTAL_TILES = WIDTH_TILES * HEIGHT_TILES;// the total number of tiles on the game board
    private static final int SIZE_TILES = 25;// the total number of tiles on the game board
    private static final int BOARD_WIDTH = WIDTH_TILES * SIZE_TILES;// the total number of tiles on the game board

    private Transition foundFood, foundHome, foundWater, foundPoison;// transitions used with the FSM
    private State foodSearch, homeSearch, waterSearch;// states used with the FSM
    private ArrayList<State> states;// holds all the states used with the FSM
    private Graph graph = new Graph();// the graph used to represent the org.hzt.ant.Game space

    private ArrayList<Ant> ants = new ArrayList<Ant>();// holds all the ants in the colony
    private boolean antHillAdded = false;// represents whether or not the ant hill has been placed
    //private ArrayList<org.hzt.ant.FiniteStateMachine> fsms = new ArrayList<org.hzt.ant.FiniteStateMachine>();// the FSMs used - one for each ant
    private Map<Ant, FiniteStateMachine> antFSM = new HashMap<Ant, FiniteStateMachine>(); // holds each set of org.hzt.ant.Ant/FSM combinations
    private final Node[] nodes = new Node[TOTAL_TILES];// used to represent each node/tile in the org.hzt.ant.Game space
    private final String[] icons = {"food.png", "water.png", "terrain.png", "poison.png"};// the different images used for the tiles
    private final String[] occupants = {"food", "water", "terrain", "poison"};// the different objects used in the game
    private final Image food;
    private final Image water;
    private final Image terrain;
    private final Image poison;
    private final Image ant;
    private final Image antHill;
    private final Image antWithFood;
    private final Image antInWater;
    private final Image antInHill;// images used in the game

    private final JPanel buttonPane = new JPanel(new GridLayout(1, 3));// adds a JPanel to hold the buttons
    private final JButton execute = new JButton("Start");// the button to execute the ant colony
    private final JButton blank = new JButton("Blank");// the button to start over

    // org.hzt.ant.Board constructor - no parameters necessary
    public Board() {
        addMouseListener(this);// adds a Mouse listener on the game space
        setFocusable(true);// allows focusable
        setPreferredSize(new Dimension(BOARD_WIDTH, 770));// sets the preferred size of the game space window
        setBackground(Color.WHITE);// sets the background color of the display to white
        setDoubleBuffered(true);// sets double buffered to true
        setLayout(new BorderLayout());// creates the layout for the game panel

        execute.addActionListener(e -> start());// adds a listener for the execute button
        blank.addActionListener(e -> makeBlank());// adds a listener for the blank button
        buttonPane.setLayout(new GridLayout(1, 2));// sets the layout for the buttonPane JPanel pane
        buttonPane.add(blank);// adds the fullBoard button to the buttonPane
        buttonPane.add(execute);// adds the execute button to the buttonPane
        add(buttonPane, BorderLayout.SOUTH);// adds the buttonPane to the JPanel displaying the game

        for (int i = 0; i < nodes.length; i++)// loops through the game nodes
            nodes[i] = new Node();// fills each element with a blank node to start

        food = getImageIcon("/images/food.png").getImage();// creates the food image
        water = getImageIcon("/images/water.png").getImage();// creates the water image
        terrain = getImageIcon("/images/terrain.png").getImage();// creates the terrain image
        poison = getImageIcon("/images/poison.png").getImage();// creates the poison image
        ant = getImageIcon("/images/ant.png").getImage();// creates the ant image
        antHill = getImageIcon("/images/antHill.png").getImage();// creates the antHill image
        antWithFood = getImageIcon("/images/antWithFood.png").getImage();// creates the antWithFood image
        antInWater = getImageIcon("/images/antInWater.png").getImage();// creates the antInWater image
        antInHill = getImageIcon("/images/antInHill.png").getImage();// creates the antInHill image

        blank.doClick();// calls the method to produce a blank board to start

        timer = new Timer(DELAY, e -> repaint());// creates timer with between-event DELAY
        timer.start();// starts the timer
    }// org.hzt.ant.Board() constructor

    private static ImageIcon getImageIcon(final String name) {
        final var resource = Optional.ofNullable(Board.class.getResource(name))
                .map(URL::getFile)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + name));
        return new ImageIcon(resource);
    }

    @Override // paintComponent method - performs duties to paint org.hzt.ant.Board
    public void paintComponent(Graphics g) {
        super.paintComponent(g);// calls JPanel paintComponent
        doDrawing(g);// calls the doDrawing method to get graphics information about objects in the org.hzt.ant.Game
        Toolkit.getDefaultToolkit().sync();// synchronizes the toolkit state
    }// paintComponent(Graphics) method

    // doDrawing method - gets graphics information for each object in the org.hzt.ant.Game
    private void doDrawing(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;// converts Graphics g to Graphics2D
        for (int i = 0; i < WIDTH_TILES; i++) {// loops through x-coords
            for (int j = 0; j < HEIGHT_TILES; j++) {// loops through y-coords
                g2d.drawImage(nodes[i * WIDTH_TILES + j].getImage(), nodes[i * WIDTH_TILES + j].getTile().x, nodes[i * HEIGHT_TILES + j].getTile().y, this);// draws each tile with its current information
            }// for (j)
        }// for (i)

        if (running) {// checks whether the simulation is running
            if (start++ % 10 == 0) {// staggers the movement of the ants, slows things down
                moveAnts();// calls the method that governs the movement of the ants
            }// if (start)
        }// if (running)
    }// doDrawing(Graphics) method

    // moveAnts method - governs the movement of the ants, with the FSM
    public synchronized void moveAnts() {
        LOGGER.info("Moving ants...");
        ArrayList<Ant> deadAnts = new ArrayList<Ant>();// holds all ants killed during current loop
        ArrayList<Integer> bornAnts = new ArrayList<Integer>();// holds all ants
        for (int i = 0; i < ants.size(); i++) {// loops through all the ants
            int deadAnt = 999999;// determines whether an ant has died and holds the index
            int antBorn = 999999;// determines whether an ant has been born and holds the index

            Ant iAnt = ants.get(i);// creates duplicate of each Spider, necessary for deletion purposes

            if (iAnt.getActions().isEmpty())// checks if the org.hzt.ant.Ant has any current actions to complete
                iAnt.setActions(antFSM.get(iAnt).update());// if not, gets actions from FSM

            String action = iAnt.getActions().remove(0);// gets the first action in the list

            int newPosition = iAnt.getCurrent();// will hold the next position of the ant
            boolean alive = true;// used to flag ant deaths
            antFSM.get(iAnt).getCurrentState().getTransition(foundFood).setTriggered(false);// sets the found food trigger to false
            antFSM.get(iAnt).getCurrentState().getTransition(foundHome).setTriggered(false);// sets the found food trigger to false
            antFSM.get(iAnt).getCurrentState().getTransition(foundWater).setTriggered(false);// sets the found food trigger to false

            switch (action) {// switch handles the different actions registered by the FSM
                case ("Died"):
                    nodes[iAnt.getCurrent()].setImage(terrain);// sets the node image to terrain
                    nodes[iAnt.getCurrent()].setOccupant("terrain");// sets the node occupant to terrain
                    alive = false;// flags the ant as dead
                    break;// breaks from switch
                case ("SearchForFood"):
                    newPosition = wander(iAnt);// gets new position through wander method
                    nodes[iAnt.getCurrent()].setImage(getImageIcon("/images/" + nodes[iAnt.getCurrent()].getOccupant() + ".png").getImage());// sets current node image as previous occupant
                    iAnt.setCurrent(newPosition);// updates the ant position
                    nodes[iAnt.getCurrent()].setImage(ant);// sets the new node image as the ant
                    if (nodes[newPosition].getOccupant().equals("food")) {// checks if food has been found
                        antFSM.get(iAnt).getCurrentState().getTransition(foundFood).setTriggered(true);// updates foundFood trigger
                    }// if (food)
                    break;// breaks from switch
                case ("PickupFood"):
                    nodes[iAnt.getCurrent()].setImage(antWithFood);// sets node image to ant with food
                    nodes[iAnt.getCurrent()].setOccupant("terrain");// sets node occupant to terrain
                    break;// breaks from switch
                case ("EnterAntHill"):
                    nodes[iAnt.getCurrent()].setImage(antInHill);// updates node image to ant in hill
                    break;// breaks from switch
                case ("InAntHill"):
                    nodes[iAnt.getCurrent()].setImage(antHill);// updates node image to ant hill
                    break;// breaks from switch
                case ("LeaveAntHill"):
                    nodes[iAnt.getCurrent()].setImage(antInHill);// updates node image to ant in hill
                    break;// breaks from switch
                case ("SearchForHome"):
                    newPosition = getAStar(iAnt);// gets new position from A*
                    nodes[iAnt.getCurrent()].setImage(getImageIcon("/images/" + nodes[iAnt.getCurrent()].getOccupant() + ".png").getImage());// updates old node image to current occupant
                    iAnt.setCurrent(newPosition);// updates ant position
                    nodes[iAnt.getCurrent()].setImage(antWithFood);// updates new node image to ant with food
                    if (iAnt.getHome() == newPosition) {// if home has been found
                        antBorn = iAnt.getHome();// gets node for new ant
                        antFSM.get(iAnt).getCurrentState().getTransition(foundHome).setTriggered(true);// sets found home trigger to true
                        nodes[iAnt.getCurrent()].setImage(antInHill);// sets new node image to ant in hill
                    }// if (home)
                    break;// breaks from switch
                case ("SearchForWater"):
                    newPosition = wander(iAnt);// gets new position from wander
                    nodes[iAnt.getCurrent()].setImage(getImageIcon("/images/" + nodes[iAnt.getCurrent()].getOccupant() + ".png").getImage());// updates old node to previous occupant image
                    iAnt.setCurrent(newPosition);// updates ant position
                    nodes[iAnt.getCurrent()].setImage(ant);// updates new node image to ant
                    if (nodes[newPosition].getOccupant().equals("water")) {// checks if water has been found
                        antFSM.get(iAnt).getCurrentState().getTransition(foundWater).setTriggered(true);// sets found water trigger to true
                    }// if (water)
                    break;// breaks from switch
                case ("DrinkWater"):
                    nodes[iAnt.getCurrent()].setImage(antInWater);// updates node image to ant in water
                    nodes[iAnt.getCurrent()].setOccupant("terrain");// updates node occupant to terrain
                    break;// breaks from switch
                default:
                    break;// breaks from switch
            }// switch(action)

            if (nodes[newPosition].getOccupant().equals("poison")) {// checks if ant steps in poison
                nodes[iAnt.getCurrent()].setImage(terrain);// updates the node image to terrain
                nodes[iAnt.getCurrent()].setOccupant("terrain");// updates the node occupant to terrain
                alive = false;// flags that ant was killed
                deadAnt = i;// gets number of ant killed
            }// if (poison)

            if (alive) {// checks if ant is alive
                if (ants.get(i).getCurrent() == ants.get(i).getHome() && !action.equals("EnterAntHill") && !action.equals("LeaveAntHill") && !action.equals("SearchForHome"))// checks if ant hill image needs to be added
                    nodes[ants.get(i).getCurrent()].setImage(getImageIcon("/images/" + "antHill.png").getImage());//sets image to ant hill
            }// if (alive)

            if (deadAnt != 999999) {// checks if current ant was killed
                deadAnts.add(iAnt);// adds number of ant killed
            }// if (ant died)

            if (antBorn != 999999) {// check if an ant was born
                bornAnts.add(antBorn);// adds number of ant born
            }// if (ant born)
        }// for (ants)

        for (Ant ant : deadAnts)// loops through all ants killed in current game loop
            ants.remove(ant);// removes ants killed during game loop

        for (Integer node : bornAnts)// loops through all ant born in current game loop
            addAnt(node, node);// adds ants born during game loop
    }// moveAnts() method

    // getAStar method - calls the A* algorithm to help the org.hzt.ant.Ant find its way home
    public int getAStar(Ant ant) {
        ArrayList<Connection> path = AStar.pathFindAStar(graph, ant.getCurrent(), ant.getHome(), new Heuristic());// creates a new path from the A* result
        ArrayList<Integer> nodesTo = new ArrayList<Integer>();// list to hold the toNodes on the path
        for (Connection connection : path)// loops through the path
            nodesTo.add(connection.getToNode());// adds the toNodes to the list
        return nodesTo.remove(0);// returns the first node
    }// getAStar(org.hzt.ant.Ant) method

    // wander mathod - moves the org.hzt.ant.Ant randomly, even odds between all current connections
    public int wander(Ant iAnt) {
        Random random = new Random();// creates random object
        int num = random.nextInt(graph.getConnections(iAnt.getCurrent()).size());// gets a random int between 0 and num of connections currently
        return graph.getConnections(iAnt.getCurrent()).get(num).getToNode();// returns the random connection to move ant
    }// wander(org.hzt.ant.Ant) method

    private void makeBlank() {
        LOGGER.info("Make blank");
        ants = new ArrayList<Ant>();// resets the list of ants
        antFSM = new HashMap<Ant, FiniteStateMachine>();// resets the fsms
        antHillAdded = false;// sets the ant hill added to false
        blank();// calls the method to empty the board
        execute.setEnabled(true);// enables the execute button
        repaint();// repaints with current information
    }

    private void start() {
        LOGGER.info("Start");
        if (antHillAdded) {// makes sure an ant hill has been added
            randomize();// calls the randomize method to fill the board with random tiles
            execute();// calls the method to execute the search
            execute.setEnabled(false);// disables execute button
        }// if (antHillAdded)
        repaint();// repaints with current information
    }

    // blank method - creates a blank board for the user to manually specify
    public void blank() {
        running = false;// sets the simulation to stopped
        for (int i = 0; i < TOTAL_TILES; i++)// loops through all tiles
            nodes[i] = new Node(new Point(i / WIDTH_TILES * SIZE_TILES, i % HEIGHT_TILES * SIZE_TILES), getImageIcon("/images/" + "empty.png").getImage(), false, "empty");// sets empty tile
    }// blank() method

    // randomize method - no parameters, fills tiles with random entities
    public void randomize() {
        Random random = new Random();// random generator used to get random int
        int r;// will hold the random int
        int num;// will hold the filtered random int that determines with entity to use for a tile
        for (int i = 0; i < WIDTH_TILES; i++) {// loops x-coords
            for (int j = 0; j < HEIGHT_TILES; j++) {// loops y-coords
                r = random.nextInt(32);// gets random int from 0 to 32
                if (r < 4) num = 0;
                else if (r < 8) num = 1;
                else if (r < 31) num = 2;
                else num = 3;// distributes different objects
                if (!nodes[i * WIDTH_TILES + j].isOccupied()) {// if tile empty or random chosen
                    nodes[i * WIDTH_TILES + j] = new Node(new Point(i * BOARD_WIDTH / WIDTH_TILES, j * BOARD_WIDTH / HEIGHT_TILES), getImageIcon("/images/" + icons[num]).getImage(), true, occupants[num]);// creates random tile
                }// if (random)
            }// for (j)
        }// for (i)
    }// randomize() method

    // execute method - performs the A* search on the board
    public void execute() {
        graph = new Graph();
        for (int i = 0; i < WIDTH_TILES; i++) {// loops x-coords
            for (int j = 0; j < HEIGHT_TILES; j++) {// loops y-coords
                addNeighbours(i, j);// adds neighbours/connections for each node
            }// for (j)
        }// for (i)

        running = true;// sets the simulation to running
    }// execute() method

    // addNeighbours method - x-coord, y-coord, cost; sets a tiles neighbours
    public void addNeighbours(int i, int j) {
        int fromNode;// origin node of the connection
        int toNode;// destination node of the connection
        ArrayList<Connection> connections = new ArrayList<Connection>();// will hold the connections
        fromNode = i * WIDTH_TILES + j;// calculates the from node by the coordinates
        if (i > 0 && j > 0) {// not on left or top game border
            toNode = (i - 1) * WIDTH_TILES + (j - 1);// calculates to node of connection from coords
            connections.add(new Connection(fromNode, toNode, 0));// add new connection
        }// if (border)
        if (i > 0) {// not on left game border
            toNode = (i - 1) * WIDTH_TILES + (j);// calculates to node of connection from coords
            connections.add(new Connection(fromNode, toNode, 0));// add new connection
        }// if (border)
        if (i > 0 && j < (WIDTH_TILES - 1)) {// not on left or bottom game border
            toNode = (i - 1) * WIDTH_TILES + (j + 1);// calculates to node of connection from coords
            connections.add(new Connection(fromNode, toNode, 0));// add new connection
        }// if (border)
        if (j > 0) {// not on top game border
            toNode = (i) * WIDTH_TILES + (j - 1);// calculates to node of connection from coords
            connections.add(new Connection(fromNode, toNode, 0));// add new connection
        }// if (border)
        if (j < (WIDTH_TILES - 1)) {// not on bottom game border
            toNode = (i) * WIDTH_TILES + (j + 1);// calculates to node of connection from coords
            connections.add(new Connection(fromNode, toNode, 0));// add new connection
        }// if (border)
        if (i < (WIDTH_TILES - 1) && j > 0) {// not on right or top game border
            toNode = (i + 1) * WIDTH_TILES + (j - 1);// calculates to node of connection from coords
            connections.add(new Connection(fromNode, toNode, 0));// add new connection
        }// if (border)
        if (i < (WIDTH_TILES - 1)) {// not on right game border
            toNode = (i + 1) * WIDTH_TILES + (j);// calculates to node of connection from coords
            connections.add(new Connection(fromNode, toNode, 0));// add new connection
        }// if (border)
        if (i < (WIDTH_TILES - 1) && j < (WIDTH_TILES - 1)) {// not on right or bottom game border
            toNode = (i + 1) * WIDTH_TILES + (j + 1);// calculates to node of connection from coords
            connections.add(new Connection(fromNode, toNode, 0));// add new connection
        }// if (border)
        graph.addConnections(connectionList(connections));// adds the connections for the current node to the graph
    }// addNeighbours(int, int, int) method

    // connectionList method - connections, copies elements from one list and returns the results in another
    public ArrayList<Connection> connectionList(ArrayList<Connection> connection) {
        ArrayList<Connection> result = new ArrayList<Connection>();// will hold list to return
        for (int i = 0; i < connection.size(); i++)// loops through all connection in list
            result.add(connection.get(i));// copies each connection into new list
        return result;// returns result list
    }// connectionList(ArrayList<org.hzt.ant.Connection>) method

    // addAnt method - takes care of the details involved with adding ants to the colony
    public void addAnt(int x, int y) {
        foundFood = new Transition("PickupFood", false);// initializes the foundFood transition
        foundHome = new Transition("InAntHill", false);// initializes the foundHome transition
        foundWater = new Transition("DrinkWater", false);// initializes the foundWater transition
        foundPoison = new Transition("Died", false);// initializes the foundPoison transition

        foodSearch = new State("SearchForFood", "DrinkWater", "PickupFood", new Transition[]{foundFood, foundPoison});
        // initializes the foodSearch state
        homeSearch = new State("SearchForHome", "PickUpFood", "EnterAntHill", new Transition[]{foundHome, foundPoison});
        // initializes the homeSearch state
        waterSearch = new State("SearchForWater", "LeaveAntHill", "DrinkWater", new Transition[]{foundWater, foundPoison});
        // initializes the waterSearch state

        foundFood.setTargetState(homeSearch);// adds the homeSearch as the target state for foundFood
        foundHome.setTargetState(waterSearch);// adds the waterSearch as the target state for foundHome
        foundWater.setTargetState(foodSearch);// adds the foodSearch as the target state for foundWater
        foundPoison.setTargetState(null);// adds the homeSearch as the target state for foundPoison

        states = new ArrayList<State>();// initializes the states collection
        states.add(foodSearch);// adds the foodSearch state
        states.add(homeSearch);// adds the homeSearch state
        states.add(waterSearch);// adds the waterSearch state

        FiniteStateMachine fsm = new FiniteStateMachine(states, foodSearch);// creates a new FSM to pair with the ant
        Ant ant = new Ant(x, y);// creates a new instance of the org.hzt.ant.Ant
        ants.add(ant);// home, current
        antFSM.put(ant, fsm);// creates an ant/fsm pair
    }// addAnt(int, int) method

    // mousePressed method - handles mouse click events
    public void mousePressed(MouseEvent e) {
        Point position = e.getPoint();// gets coordinates of the click
        if (position.x < BOARD_WIDTH && position.y < BOARD_WIDTH && !running) {// determines if the click is within the bounds of the game space
            int x = (int) Math.floor(position.x / SIZE_TILES);// gets the x tile value
            int y = (int) Math.floor(position.y / SIZE_TILES);// gets the y tile value
            Point point = new Point(position.x - position.x % SIZE_TILES, position.y - position.y % SIZE_TILES);// gets the uper left coordinate of the tile
            if (!nodes[x * WIDTH_TILES + y].isOccupied() && !antHillAdded) {// checks if an ant needs to be added
                addAnt(x * WIDTH_TILES + y, x * WIDTH_TILES + y);// calls the add ant method
                nodes[x * WIDTH_TILES + y] = new Node(point, getImageIcon("/images/" + "antHill.png").getImage(), true, "antHill");// adds the antHill at specified node
                antHillAdded = true;// marks anthill flag as created
            } else if (nodes[x * WIDTH_TILES + y].isOccupied()) {// allows additional ants to be added to the colony
                addAnt(x * WIDTH_TILES + y, x * WIDTH_TILES + y);// calls addAnt method
            }// if (anthill)
        }// if (within bounds)
    }// mousePressed(MouseEvent) method

    // must be present for MouseListener
    public void mouseReleased(MouseEvent e) {
    }// mouseReleased(MouseEvent) method

    // must be present for MouseListener
    public void mouseEntered(MouseEvent e) {
    }// mouseEntered(MouseEvent) method

    // must be present for MouseListener
    public void mouseExited(MouseEvent e) {
    }//  mouseExited(MouseEvent) method

    // must be present for MouseListener
    public void mouseClicked(MouseEvent e) {
    }// mouseClicked(MouseEvent) method
}// org.hzt.ant.Board class
