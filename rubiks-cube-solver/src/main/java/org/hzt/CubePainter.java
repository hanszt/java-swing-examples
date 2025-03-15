package org.hzt;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Copyright 2017, Shoumyo Chakravorti, All rights reserved.
 * <p>
 * Licensed under the MIT License.
 * <p>
 * The CubePainter class defines and takes input from all components which the user can interact with.
 * The CubePainter class has two unique solution modes: "Text Scramble" and "Color Selection".
 *
 * @author Shoumyo Chakravorti
 * @version 2.0
 */
public final class CubePainter extends JPanel {
    //Auto-generated ID
    private static final long serialVersionUID = -8879300942801280752L;

    private final RandomGenerator random;
    //Buttons to start and stop animation; to reset the scramble based on text field
    private JButton start, stop, applyScramble, randomize;
    private JButton skip, rewind;
    //Buttons used during the color input phase to either reset the colors or proceed with the inputed colors
    //to the solution
    private JButton resetCubeInputs, setInputs;
    //Slider to control animation speed
    private JSlider animSpeed;
    //Allows User to choose which side's colors to enter during color input mode
    private JComboBox<String> sideChooser;
    private String[] instructions; //Colors for instructions to display during color input mode
    //Text field to allow user to input a custom scramble different from the default scramble
    private JTextField inputScramble;
    //Timer to control delay between animation of moves
    private final Timer frameTimer;
    //Stroke for bold outline along edges of cubie colors
    final static BasicStroke s = new BasicStroke(5.0f, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER, 10.0f);
    private final static Font font = new Font("Monospace", Font.BOLD, 35);
    //Standard frame rate delay
    public final static int DELAY = 1500;
    final static int CUBIE_SIZE = 50;

    //Allows for toggling between modes when updateMode() is invoked
    private String mode = "";
    public final static String TEXT_SCRAMBLE = "Text Scramble";
    public final static String COLOR_SELECTION = "Color Selection";
    private char colorSelected; //The color selected while in color input mode
    private char sideChosen; //The side for which the user is entering colors
    /*
     * colorsInputed[0] = left colors
     * colorsInputed[1] = up colors
     * colorsInputed[2] = front colors
     * colorsInputed[3] = back colors
     * colorsInputed[4] = right colors
     * colorsInputed[5] = down colors
     */
    private final char[][][] colorsInputed; //Holds all inputed colors
    //Whether a solution is currently being displayed
    private boolean inSolution;

    private Cube cube = new Cube();
    //Default scramble
    private final String DEFAULT_SCRAMBLE = "F2 D' B U' D L2 B2 R B L' B2 L2 B2 D' R2 F2 D' R2 U' ";
    private String scramble = DEFAULT_SCRAMBLE,
            sunflower = "", whiteCross = "",
            whiteCorners = "", secondLayer = "",
            yellowCross = "", OLL = "", PLL = "";
    private String movesToPerform = "", movesPerformed = "";

    /*
     * Respective stages of the solution w.r.t the phase variable
     * 0 = sunflower
     * 1 = whiteCross
     * 2 = whiteCorners
     * 3 = secondLayer
     * 4 = yellowCross
     * 5 = OLL
     * 6 = PLL
     * The phase is updated in updatePhase() to reflect the stage at which the solution is
     */
    private int phase = 0;
    private String phaseString;
    //Helps keep track of moves to perform, and allows for painting of moves
    private int movesIndex = 0;

    /**
     * Initializes all elements of the CubePainter JPanel with which the user can interact.
     * This includes all buttons, sliders, and text fields.
     */
    public CubePainter(final RandomGenerator randomGenerator) {
        this.random = randomGenerator;
        setLayout(null); //Allows for manually setting locations of components
        setSize(getPreferredSize());
        setIgnoreRepaint(true);
        setVisible(true);
        mode = TEXT_SCRAMBLE;
        inSolution = true;
        phaseString = "Sunflower";
        colorSelected = 'R';
        instructions = new String[]{"Red", "Yellow", "White"};
        sideChosen = 'L';
        colorsInputed = new char[6][3][3];
        resetCubeInputs();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                CubePainter.this.selectColor(e);
            }

            @Override
            public void mousePressed(final MouseEvent e) {
                CubePainter.this.selectColor(e);
            }
        });

        //Initialize all buttons, sliders and text fields
        initializeComponents();
        resetScramble(inputScramble.getText());
        //Initialize the frame timer
        frameTimer = new Timer(CubePainter.DELAY, e -> {
            if (inSolution) {
                performNextMove();
                repaint();
            }
        });
    }

    /**
     * Resets the colors inputed in color selection mode to the colors of a cube in its solved state.
     */
    public void resetCubeInputs() {
        for (var i = 0; i < 3; i++) {
            Arrays.fill(colorsInputed[0][i], 'R');
            Arrays.fill(colorsInputed[1][i], 'Y');
            Arrays.fill(colorsInputed[2][i], 'G');
            Arrays.fill(colorsInputed[3][i], 'B');
            Arrays.fill(colorsInputed[4][i], 'O');
            Arrays.fill(colorsInputed[5][i], 'W');
        }
    }

    /**
     * Initializes all the buttons, sliders, combo boxes, and text fields for the user to interact with.
     * Helper methods for constructor.
     */
    public void initializeComponents() {
        start = new JButton("Start");
        start.setLocation(50, 10);
        start.setSize(60, 20);
        add(start);
        start.addActionListener(unused -> frameTimer.start());

        stop = new JButton("Stop");
        stop.setLocation(130, 10);
        stop.setSize(60, 20);
        add(stop);
        stop.addActionListener(unused -> frameTimer.stop());

        final ImageIcon icon1, icon2;
        try {
            Image img1 = ImageIO.read(IO.resourceUrl("/images/Skip.png"));
            Image img2 = ImageIO.read(IO.resourceUrl("/images/Rewind.png"));
            img1 = img1.getScaledInstance(25, 25, Image.SCALE_SMOOTH);
            img2 = img2.getScaledInstance(25, 25, Image.SCALE_SMOOTH);
            icon1 = new ImageIcon(img1);
            icon2 = new ImageIcon(img2);
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }

        skip = new JButton(icon1);
        skip.setLocation(240, 8);
        skip.setSize(icon1.getIconWidth(), icon1.getIconHeight());
        skip.setBackground(this.getBackground());
        skip.setBorder(null);
        skip.addActionListener(unused -> {
            performNextMove();
            repaint();
        });
        add(skip);

        rewind = new JButton(icon2);
        rewind.setLocation(210, 8);
        rewind.setSize(icon2.getIconWidth(), icon2.getIconHeight());
        rewind.setBackground(this.getBackground());
        rewind.setBorder(null);
        rewind.addActionListener(unused -> rewind());
        add(rewind);

        animSpeed = new JSlider(1, 20);
        animSpeed.setValue(10); //Slider values range from 1 to 10
        animSpeed.setMinorTickSpacing(1);
        animSpeed.setPaintTicks(true);
        animSpeed.setSnapToTicks(true);
        animSpeed.setLocation(500, 0);
        animSpeed.setSize(200, 40);
        add(animSpeed);
        animSpeed.addChangeListener(e -> {
            if (e.getSource() == animSpeed) {
                frameTimer.setDelay(DELAY / animSpeed.getValue());
            }
        });

        inputScramble = new JTextField(scramble);
        inputScramble.setLocation(170, 40);
        inputScramble.setSize(400, 40);
        inputScramble.setFocusable(true);
        inputScramble.setBorder(BorderFactory.createLineBorder(Color.black));
        inputScramble.setFont(new Font("Monospace", Font.BOLD, 15));
        add(inputScramble);

        applyScramble = new JButton("APPLY");
        applyScramble.setLocation(590, 40);
        applyScramble.setSize(100, 20);
        add(applyScramble);
        applyScramble.addActionListener(unused -> applyScramble());

        randomize = new JButton("RANDOM");
        randomize.setLocation(590, 70);
        randomize.setSize(100, 20);
        add(randomize);
        randomize.addActionListener(unused -> randomize());

        sideChooser = new JComboBox<>(new String[]{"Left", "Up", "Back", "Front", "Right", "Down"});
        sideChooser.setLocation(270, 50);
        sideChooser.setSize(100, 30);
        add(sideChooser);
        sideChooser.addActionListener(unused -> {
            sideChosen = ((String) Objects.requireNonNull(sideChooser.getSelectedItem())).charAt(0);
            instructions = getInstructions();
            repaint();
        });
        sideChooser.setVisible(false);
        sideChooser.setEnabled(false);

        resetCubeInputs = new JButton("RESET");
        resetCubeInputs.setLocation(200, 650);
        resetCubeInputs.setSize(100, 30);
        add(resetCubeInputs);
        resetCubeInputs.addActionListener(unused -> {
            resetCubeInputs();
            repaint();
        });
        resetCubeInputs.setVisible(false);
        resetCubeInputs.setEnabled(false);

        setInputs = new JButton("PROCEED");
        setInputs.setLocation(300, 650);
        setInputs.setSize(100, 30);
        add(setInputs);
        setInputs.addActionListener(unused -> setInputs());
        setInputs.setVisible(false);
        setInputs.setEnabled(false);
    }

    private void applyScramble() {
        frameTimer.stop();
        //While the cube is being scrambled, screen will show nonsensical colors, such as black, so set as invisible
        setVisible(false);
        resetScramble(inputScramble.getText());
        inSolution = true;
        updateElements();
        repaint();
        setVisible(true);
    }

    private void randomize() {
        cube = new Cube();
        inputScramble.setText(cube.randScramble(random));
        scramble = inputScramble.getText();
        setVisible(false);
        resetScramble(inputScramble.getText());
        inSolution = true;
        updateElements();
        repaint();
        setVisible(true);
    }

    private void setInputs() {
        frameTimer.stop();
        //While the cube is being scrambled, screen will show nonsensical colors, such as black, so set as invisible
        setVisible(false);
        cube.setAllColors(colorsInputed);
        resetScrambleByColorInputs();
        inSolution = true;
        updateElements();
        repaint();
        setVisible(true);
    }

    private void rewind() {
        var flag = false;
        final var prevIndex = movesIndex;
        while (movesIndex > 1 && !flag) {
            movesIndex--;
            if (movesToPerform.charAt(movesIndex - 1) == ' ') {
                flag = !flag;
            }
            System.out.println(movesIndex);
        }
        if (movesIndex == 1) {
            movesIndex = 0;
        }
        movesPerformed = movesToPerform.substring(0, movesIndex);
        if (movesPerformed.length() >= 35) {
            movesPerformed = movesPerformed.substring(movesPerformed.length() - 33);
        }
        cube.reverseMoves(movesToPerform.substring(movesIndex, prevIndex));
        repaint();
    }

    /**
     * Returns the preferred dimensions of the CubePainter as a Dimension object.
     *
     * @return default dimensions of CubePainter
     */
    public Dimension getPreferredSize() {
        return new Dimension(700, 770);
    }

    /**
     * Paints the JPanel. Upon initialization, paints the buttons, sliders, and text field which
     * the user can interact with. When repaint() is called, the main changes that will be visible
     * are changes to the cube, moves to be performed, and moves already performed. For painting the cube, this method
     * invokes the paintComponent() method from Cube to retrieve all colors, and after painting those colors,
     * paints an outline around the cubies.
     */
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);

        if (mode.equals(TEXT_SCRAMBLE)) {
            g.setFont(new Font("Monospace", Font.BOLD, 25));
            g.drawString("Scramble: ", 30, 70);
        }

        if (inSolution) {
            //Display the phase of a solution
            g.setFont(new Font("Monospace", Font.BOLD, 25));
            g.drawString("Phase: " + phaseString, 30, 120);

            g.setFont(font);
            g.setColor(Color.RED);
            g.drawString(movesPerformed, 50, 700); //Draw the moves that have already been performed

            //Draw the moves that are yet to be performed
            g.setColor(Color.BLACK);
            if (movesIndex <= movesToPerform.length() - 1) { //Avoid index out of bounds error
                if (movesToPerform.substring(movesIndex).length() >= 33) {
                    g.drawString(movesToPerform.substring(movesIndex, movesIndex + 33), 40, 650);
                } else {
                    g.drawString(movesToPerform.substring(movesIndex), 40, 650);
                }
            }

            //Paint the cube itself now
            ((Graphics2D) g).setStroke(s);
            cube.paintComponent(g);
        } else {
            //Paint the color selection boxes
            ((Graphics2D) g).setStroke(s);
            var xVal = 100;
            var yVal = 450;
            for (var i = 0; i < 6; i++) {
                switch (i) {
                    case (0) -> g.setColor(Color.RED);
                    case (1) -> g.setColor(Color.GREEN);
                    case (2) -> g.setColor(Color.BLUE);
                    case (3) -> g.setColor(Color.YELLOW);
                    case (4) -> g.setColor(Color.ORANGE);
                    case (5) -> g.setColor(Color.WHITE);
                }
                g.fillRect(xVal, yVal, CUBIE_SIZE, CUBIE_SIZE);
                g.setColor(Color.BLACK);
                g.drawRect(xVal, yVal, CUBIE_SIZE, CUBIE_SIZE);
                xVal += CUBIE_SIZE * 1.5;
            }

            //Paint the chosen cube side
            xVal = 250;
            yVal = 200;
            final var sideColors = colorsInputed[getIndexOfSide(sideChosen)];
            for (var i = 0; i < 3; i++) {
                for (var j = 0; j < 3; j++) {
                    g.setColor(getColor(sideColors[i][j]));
                    g.fillRect(xVal + j * CUBIE_SIZE, yVal + i * CUBIE_SIZE, CUBIE_SIZE, CUBIE_SIZE);
                    g.setColor(Color.BLACK);
                    g.drawRect(xVal + j * CUBIE_SIZE, yVal + i * CUBIE_SIZE, CUBIE_SIZE, CUBIE_SIZE);
                }
            }

            //Paint the instructions for holding the cube
            g.setColor(Color.BLACK);
            g.drawString("Hold the cube such that " + instructions[0] + " is facing up, " +
                            instructions[1] + " is to the back, and " + instructions[2] + " is in front.",
                    50, 130);
            g.drawString("Enter the top colors.",
                    50, 150);

            //Paint the color that is selected so user is sure to paint correct color
            g.setFont(font);
            g.drawString("Selected Color:", 100, 500 + CUBIE_SIZE * 2);
            g.setColor(getColor(colorSelected));
            g.fillRect(400, 465 + CUBIE_SIZE * 2, CUBIE_SIZE, CUBIE_SIZE);
            g.setColor(Color.BLACK);
            g.drawRect(400, 465 + CUBIE_SIZE * 2, CUBIE_SIZE, CUBIE_SIZE);
        }
    }

    /**
     * Returns the appropriate Color object based on a cubie's color for appropriate
     * painting in the paintComponent() method.
     *
     * @param color: cubie color
     * @return corresponding Color object
     */
    private Color getColor(final char color) {
        return switch (color) {
            case 'W' -> Color.WHITE;
            case 'Y' -> Color.YELLOW;
            case 'B' -> Color.BLUE;
            case 'G' -> Color.GREEN;
            case 'R' -> Color.RED;
            case 'O' -> Color.ORANGE;
            default -> Color.BLACK;
        };
    }

    /**
     * Gets the index for colorsInputed[(index here)] that corresponds to the side currently being painted when in color
     * selection mode. Helper method for paintComponent().
     *
     * @param side the side as character
     */
    private int getIndexOfSide(final char side) {
        return switch (side) {
            case 'L' -> 0;
            case 'U' -> 1;
            case 'F' -> 2;
            case 'B' -> 3;
            case 'R' -> 4;
            case 'D' -> 5;
            default -> 6;
        };
    }

    /**
     * Retrieves the colors of the faces to be printed in the instructions in the paintComponent() method.
     * If String[] colors = getInstructions(), color[0] is the color to hold on top, colors[1] is the color
     * to hold in the back, and colors[2] is the color to hold in front.
     *
     * @return
     */
    private String[] getInstructions() {
        final var colors = new String[3];
        switch (sideChosen) {
            case ('L') -> {
                colors[0] = "Red";
                colors[1] = "Yellow";
                colors[2] = "White";
            }
            case ('U') -> {
                colors[0] = "Yellow";
                colors[1] = "Blue";
                colors[2] = "Green";
            }
            case ('F') -> {
                colors[0] = "Green";
                colors[1] = "Yellow";
                colors[2] = "White";
            }
            case ('B') -> {
                colors[0] = "Blue";
                colors[1] = "Yellow";
                colors[2] = "White";
            }
            case ('R') -> {
                colors[0] = "Orange";
                colors[1] = "Yellow";
                colors[2] = "White";
            }
            case ('D') -> {
                colors[0] = "White";
                colors[1] = "Green";
                colors[2] = "Blue";
            }
        }
        return colors;
    }

    /**
     * Resets the scramble that is to be applied on the cube based on the input.
     * Determines the moves to be performed to solve the cube as well.
     *
     * @param s: the scramble to be applied
     */
    public void resetScramble(final String s) {
        scramble = s;
        cube = new Cube();
        cube.scramble(scramble);
        sunflower = cube.makeSunflower();
        whiteCross = cube.makeWhiteCross();
        whiteCorners = cube.finishWhiteLayer();
        secondLayer = cube.insertAllEdges();
        yellowCross = cube.makeYellowCross();
        OLL = cube.orientLastLayer();
        PLL = cube.permuteLastLayer();

        movesToPerform = sunflower;
        movesPerformed = "";

        cube = new Cube();
        cube.scramble(scramble);
        //If the cube is being scrambled newly after initializing is complete and animation has begun,
        //be sure to reset all reference indexes
        movesIndex = 0;
        phase = 0;
        phaseString = "Sunflower";
        repaint();
    }

    /**
     * After the user inputs their desired colors in color selection mode, pressing the setInputs button
     * will invoke this method, acquiring the required moves necessary to solve the cube. The cube is restored back to
     * the scrambled state after the solution moves are acquired.
     */
    public void resetScrambleByColorInputs() {
        cube.setAllColors(colorsInputed);
        sunflower = cube.makeSunflower();
        whiteCross = cube.makeWhiteCross();
        whiteCorners = cube.finishWhiteLayer();
        secondLayer = cube.insertAllEdges();
        yellowCross = cube.makeYellowCross();
        OLL = cube.orientLastLayer();
        PLL = cube.permuteLastLayer();

        movesToPerform = sunflower;
        movesPerformed = "";

        movesIndex = 0;
        phase = 0;
        phaseString = "Sunflower";
        cube.setAllColors(colorsInputed); //Reset the cube to scrambled state
        repaint();
    }


    /**
     * After updating the phase (if necessary), performs the next move in the String movesToPerform
     * and updates movesPerformed.
     */
    public void performNextMove() {
        updatePhase();

        //Get to a character that is not a space
        while (movesIndex < movesToPerform.length() - 1 && movesToPerform.substring(movesIndex, movesIndex + 1).compareTo(" ") == 0) {
            movesIndex++;
        }
        //Same logic as in Cube class's performMoves() method
        if (!movesToPerform.isEmpty() && movesToPerform.charAt(movesIndex) != ' ') {
            if (movesIndex != movesToPerform.length() - 1) {
                if (movesToPerform.substring(movesIndex + 1, movesIndex + 2).compareTo("2") == 0) {
                    //Turning twice ex. U2
                    cube.turn(movesToPerform.substring(movesIndex, movesIndex + 1));
                    cube.turn(movesToPerform.substring(movesIndex, movesIndex + 1));
                    movesIndex++;
                } else if (movesToPerform.substring(movesIndex + 1, movesIndex + 2).compareTo("'") == 0) {
                    //Making a counterclockwise turn ex. U'
                    cube.turn(movesToPerform.substring(movesIndex, movesIndex + 2));
                    movesIndex++;
                } else {
                    //Clockwise turn
                    cube.turn(movesToPerform.substring(movesIndex, movesIndex + 1));
                }
            } else {
                //Clockwise turn
                cube.turn(movesToPerform.substring(movesIndex, movesIndex + 1));
            }
        }
        movesIndex++;
        //Append the moves performed onto the end of movesPerformed
        if (!movesToPerform.isEmpty()) {
            movesPerformed = movesToPerform.substring(0, movesIndex);
        }
        //Ensure that movesPerformed does not overflow out of the graphical interface
        if (movesPerformed.length() >= 35) {
            movesPerformed = movesPerformed.substring(movesPerformed.length() - 33);
        }
    }


    /**
     * Updates the UI elements that the user can interact with depending on the current mode and whether
     * a solution is being played.
     */
    public void updateElements() {
        if (mode.equals(TEXT_SCRAMBLE)) {
            start.setEnabled(true);
            start.setVisible(true);
            stop.setEnabled(true);
            stop.setVisible(true);
            animSpeed.setEnabled(true);
            animSpeed.setVisible(true);
            inputScramble.setEnabled(true);
            inputScramble.setVisible(true);
            applyScramble.setEnabled(true);
            applyScramble.setVisible(true);
            skip.setEnabled(true);
            skip.setVisible(true);
            rewind.setEnabled(true);
            rewind.setVisible(true);
            randomize.setEnabled(true);
            randomize.setVisible(true);

            //Disable all components specific to color selection mode
            sideChooser.setVisible(false);
            sideChooser.setEnabled(false);
            resetCubeInputs.setVisible(false);
            resetCubeInputs.setEnabled(false);
            setInputs.setVisible(false);
            setInputs.setEnabled(false);
        } else if (mode.equals(COLOR_SELECTION)) {
            if (inSolution) {
                start.setEnabled(true);
                start.setVisible(true);
                stop.setEnabled(true);
                stop.setVisible(true);
                animSpeed.setEnabled(true);
                animSpeed.setVisible(true);
                skip.setEnabled(true);
                skip.setVisible(true);
                rewind.setEnabled(true);
                rewind.setVisible(true);

                randomize.setEnabled(false);
                randomize.setVisible(false);
                sideChooser.setVisible(false);
                sideChooser.setEnabled(false);
                resetCubeInputs.setVisible(false);
                resetCubeInputs.setEnabled(false);
                setInputs.setVisible(false);
                setInputs.setEnabled(false);
            } else if (!inSolution) {
                start.setEnabled(false);
                start.setVisible(false);
                stop.setEnabled(false);
                stop.setVisible(false);
                animSpeed.setEnabled(false);
                animSpeed.setVisible(false);
                randomize.setEnabled(false);
                randomize.setVisible(false);

                skip.setEnabled(false);
                skip.setVisible(false);
                rewind.setEnabled(false);
                rewind.setVisible(false);
                sideChooser.setVisible(true);
                sideChooser.setEnabled(true);
                resetCubeInputs.setVisible(true);
                resetCubeInputs.setEnabled(true);
                setInputs.setVisible(true);
                setInputs.setEnabled(true);
            }
            //Disable all components specific to text scramble mode
            inputScramble.setEnabled(false);
            inputScramble.setVisible(false);
            applyScramble.setEnabled(false);
            applyScramble.setVisible(false);
        }
    }

    /**
     * Updates the mode to either text scramble or color selection mode based on the parameter.
     *
     * @param str the mode to change to
     */
    public void updateMode(final String str) {
        if (!mode.equals(str)) {
            mode = str;
            cube = new Cube();
            if (mode.equals(TEXT_SCRAMBLE)) {
                scramble = DEFAULT_SCRAMBLE;
                resetScramble(scramble);
                inSolution = true;
            }
            updateElements();
            repaint();
        }
    }

    /**
     * Sets {@code inSolution} to the parameter, determining whether a solution is to be displayed.
     *
     * @param inSoln whether mode should be switched to being in a solution or not
     */
    public void setInSolution(final boolean inSoln) {
        inSolution = inSoln;
    }

    /**
     * Updates the current phase of the solution as necessary
     * Respective stages of the solution w.r.t the phase variable
     * 0 = sunflower		 	1 = whiteCross		2 = whiteCorners		3 = secondLayer
     * 4 = yellowCross		5 = OLL				6 = PLL
     */
    public void updatePhase() {
        if (movesIndex >= movesToPerform.length()) {
            switch (phase) {
                case 0 -> {
                    movesToPerform = whiteCross;
                    phaseString = "White Cross";
                }
                case 1 -> {
                    movesToPerform = whiteCorners;
                    phaseString = "White Corners";
                }
                case 2 -> {
                    movesToPerform = secondLayer;
                    phaseString = "Second Layer";
                }
                case 3 -> {
                    movesToPerform = yellowCross;
                    phaseString = "Yellow Cross";
                }
                case 4 -> {
                    movesToPerform = OLL;
                    phaseString = "OLL";
                }
                case 5 -> {
                    movesToPerform = PLL;
                    phaseString = "PLL";
                }
                case 6 -> {
                    movesToPerform = " ";
                    phaseString = "Solved";
                    phase--;
                    frameTimer.stop();
                }
            }
            phase++;
            movesIndex = 0;
        }
    }

    /**
     * Takes in mouse inputs during color selection mode for selecting and inputting colors
     */
    private void selectColor(final MouseEvent e) {
        if (mode.equals(COLOR_SELECTION) && !inSolution) {
            if (e.getY() > 200 && e.getY() < 200 + CUBIE_SIZE * 3) {
                final var i = (e.getY() - 200) / CUBIE_SIZE;
                final var j = (e.getX() - 250) / CUBIE_SIZE;
                colorsInputed[getIndexOfSide(sideChosen)][i][j] = colorSelected;
                repaint();
            } else if (e.getY() > 450 && e.getY() < 450 + CUBIE_SIZE) {
                final var image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                final var g2 = image.createGraphics();
                this.paint(g2);
                final var color = image.getRGB(e.getX(), e.getY());
                g2.dispose();
                colorSelected = switch (color) {
                    //Red
                    case (-65536) -> 'R';
                    //Green
                    case (-16711936) -> 'G';
                    //Blue
                    case (-16776961) -> 'B';
                    //Yellow
                    case (-256) -> 'Y';
                    //Orange
                    case (-14336) -> 'O';
                    //White
                    case (-1) -> 'W';
                    default -> throw new IllegalStateException("Unexpected value: " + color);
                };
                repaint();
            }
        }
    }
}
