package org.hzt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import static org.hzt.CubePainter.Mode.TEXT_SCRAMBLE;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(CubePainter.class);
    private static final String DEFAULT_SCRAMBLE = "F2 D' B U' D L2 B2 R B L' B2 L2 B2 D' R2 F2 D' R2 U' ";
    //Stroke for bold outline along edges of cubie colors
    final static BasicStroke s = new BasicStroke(5.0f, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER, 10.0f);
    private static final Font font = new Font("Monospace", Font.BOLD, 35);
    //Standard frame rate delay
    public static final int DELAY = 1500;
    static final int CUBIE_SIZE = 50;

    private final RandomGenerator random;
    //Buttons to start and stop animation; to reset the scramble based on text field
    private final JButton startButton;
    private final JButton stopButton;
    private final JButton applyScrambleButton;
    private final JButton randomizeButton;
    private final JButton skipButton;
    private final JButton rewindButton;
    //Buttons used during the color input phase to either reset the colors or proceed with the inputed colors
    //to the solution
    private final JButton resetCubeInputsButton;
    private final JButton proceedButton;
    //Slider to control animation speed
    private final JSlider animSpeedSlider;
    //Allows User to choose which side's colors to enter during color input mode
    private final JComboBox<Side> sideChooser;
    //Text field to allow user to input a custom scramble different from the default scramble
    private final JTextField scrambleTextField;
    //Timer to control delay between animation of moves
    private final Timer frameTimer;


    private String[] instructions; //Colors for instructions to display during color input mode

    //Allows for toggling between modes when updateMode() is invoked
    private Mode mode = TEXT_SCRAMBLE;
    private char colorSelected; //The color selected while in color input mode

    enum Side {Left, Up, Front, Back, Right, Down}

    private Side sideChosen; //The side for which the user is entering colors
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

    private String scramble = DEFAULT_SCRAMBLE;
    private String sunflower = "";
    private String whiteCross = "";
    private String whiteCorners = "";
    private String secondLayer = "";
    private String yellowCross = "";
    private String OLL = "";
    private String PLL = "";
    private String movesToPerform = "";
    private String movesPerformed = "";

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

    enum Mode {TEXT_SCRAMBLE, COLOR_SELECTION}

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
        sideChosen = Side.Left;
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
        //Initialize the frame timer
        frameTimer = new Timer(CubePainter.DELAY, e -> {
            if (inSolution) {
                performNextMove();
                repaint();
            }
        });

        //Initialize all buttons, sliders and text fields
        startButton = new JButton("Start");
        startButton.setLocation(50, 10);
        startButton.setSize(60, 20);
        add(startButton);
        startButton.addActionListener(unused -> frameTimer.start());

        stopButton = new JButton("Stop");
        stopButton.setLocation(130, 10);
        stopButton.setSize(60, 20);
        add(stopButton);
        stopButton.addActionListener(unused -> frameTimer.stop());

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

        skipButton = new JButton(icon1);
        skipButton.setLocation(240, 8);
        skipButton.setSize(icon1.getIconWidth(), icon1.getIconHeight());
        skipButton.setBackground(this.getBackground());
        skipButton.setBorder(null);
        skipButton.addActionListener(unused -> {
            performNextMove();
            repaint();
        });
        add(skipButton);

        rewindButton = new JButton(icon2);
        rewindButton.setLocation(210, 8);
        rewindButton.setSize(icon2.getIconWidth(), icon2.getIconHeight());
        rewindButton.setBackground(this.getBackground());
        rewindButton.setBorder(null);
        rewindButton.addActionListener(unused -> rewind());
        add(rewindButton);

        animSpeedSlider = new JSlider(1, 20);
        animSpeedSlider.setValue(10); //Slider values range from 1 to 10
        animSpeedSlider.setMinorTickSpacing(1);
        animSpeedSlider.setPaintTicks(true);
        animSpeedSlider.setSnapToTicks(true);
        animSpeedSlider.setLocation(500, 0);
        animSpeedSlider.setSize(200, 40);
        add(animSpeedSlider);
        animSpeedSlider.addChangeListener(unused -> frameTimer.setDelay(DELAY / animSpeedSlider.getValue()));
        frameTimer.setDelay(DELAY / animSpeedSlider.getValue());

        scrambleTextField = new JTextField(scramble);
        scrambleTextField.setLocation(170, 40);
        scrambleTextField.setSize(400, 40);
        scrambleTextField.setFocusable(true);
        scrambleTextField.setBorder(BorderFactory.createLineBorder(Color.black));
        scrambleTextField.setFont(new Font("Monospace", Font.BOLD, 15));
        add(scrambleTextField);

        applyScrambleButton = new JButton("APPLY");
        applyScrambleButton.setLocation(590, 40);
        applyScrambleButton.setSize(100, 20);
        add(applyScrambleButton);
        applyScrambleButton.addActionListener(unused -> applyScramble());

        randomizeButton = new JButton("RANDOM");
        randomizeButton.setLocation(590, 70);
        randomizeButton.setSize(100, 20);
        add(randomizeButton);
        randomizeButton.addActionListener(unused -> randomize());

        sideChooser = new JComboBox<>(new Side[]{Side.Left, Side.Up, Side.Front, Side.Back, Side.Right, Side.Down});
        sideChooser.setLocation(270, 50);
        sideChooser.setSize(100, 30);
        add(sideChooser);
        sideChooser.addActionListener(unused -> {
            sideChosen = ((Side) Objects.requireNonNull(sideChooser.getSelectedItem()));
            instructions = getInstructions();
            repaint();
        });
        sideChooser.setVisible(false);
        sideChooser.setEnabled(false);

        resetCubeInputsButton = new JButton("RESET");
        resetCubeInputsButton.setLocation(200, 650);
        resetCubeInputsButton.setSize(100, 30);
        add(resetCubeInputsButton);
        resetCubeInputsButton.addActionListener(unused -> {
            resetCubeInputs();
            repaint();
        });
        resetCubeInputsButton.setVisible(false);
        resetCubeInputsButton.setEnabled(false);

        proceedButton = new JButton("PROCEED");
        proceedButton.setLocation(300, 650);
        proceedButton.setSize(100, 30);
        add(proceedButton);
        proceedButton.addActionListener(unused -> proceed());
        proceedButton.setVisible(false);
        proceedButton.setEnabled(false);
        resetScramble(scrambleTextField.getText());
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

    private void applyScramble() {
        frameTimer.stop();
        //While the cube is being scrambled, screen will show nonsensical colors, such as black, so set as invisible
        setVisible(false);
        resetScramble(scrambleTextField.getText());
        inSolution = true;
        updateElements();
        repaint();
        setVisible(true);
    }

    private void randomize() {
        cube = new Cube();
        scrambleTextField.setText(cube.randScramble(random));
        scramble = scrambleTextField.getText();
        setVisible(false);
        resetScramble(scrambleTextField.getText());
        inSolution = true;
        updateElements();
        repaint();
        setVisible(true);
    }

    private void proceed() {
        LOGGER.info("Proceeding...");
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
            LOGGER.info("Moves index {}", movesIndex);
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

        if (mode == TEXT_SCRAMBLE) {
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
                    case 0 -> g.setColor(Color.RED);
                    case 1 -> g.setColor(Color.GREEN);
                    case 2 -> g.setColor(Color.BLUE);
                    case 3 -> g.setColor(Color.YELLOW);
                    case 4 -> g.setColor(Color.ORANGE);
                    case 5 -> g.setColor(Color.WHITE);
                }
                g.fillRect(xVal, yVal, CUBIE_SIZE, CUBIE_SIZE);
                g.setColor(Color.BLACK);
                g.drawRect(xVal, yVal, CUBIE_SIZE, CUBIE_SIZE);
                xVal += (int) (CUBIE_SIZE * 1.5);
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
    private int getIndexOfSide(final Side side) {
        return switch (side) {
            case Left -> 0;
            case Up -> 1;
            case Front -> 2;
            case Back -> 3;
            case Right -> 4;
            case Down -> 5;
        };
    }

    /**
     * Retrieves the colors of the faces to be printed in the instructions in the paintComponent() method.
     * If String[] colors = getInstructions(), color[0] is the color to hold on top, colors[1] is the color
     * to hold in the back, and colors[2] is the color to hold in front.
     *
     * @return The colors of that side
     */
    private String[] getInstructions() {
        return switch (sideChosen) {
            case Left -> new String[] {"Red", "Yellow", "White"};
            case Up -> new String[] {"Yellow", "Blue", "Green"};
            case Front -> new String[] {"Green", "Yellow", "White"};
            case Back -> new String[] {"Blue", "Yellow", "White"};
            case Right -> new String[] {"Orange", "Yellow", "White"};
            case Down -> new String[] {"White", "Green", "Blue"};
        };
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
        LOGGER.info("Performing next move...");
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
        switch (mode) {
            case Mode.TEXT_SCRAMBLE -> {
                startButton.setEnabled(true);
                startButton.setVisible(true);
                stopButton.setEnabled(true);
                stopButton.setVisible(true);
                animSpeedSlider.setEnabled(true);
                animSpeedSlider.setVisible(true);
                scrambleTextField.setEnabled(true);
                scrambleTextField.setVisible(true);
                applyScrambleButton.setEnabled(true);
                applyScrambleButton.setVisible(true);
                skipButton.setEnabled(true);
                skipButton.setVisible(true);
                rewindButton.setEnabled(true);
                rewindButton.setVisible(true);
                randomizeButton.setEnabled(true);
                randomizeButton.setVisible(true);

                //Disable all components specific to color selection mode
                sideChooser.setVisible(false);
                sideChooser.setEnabled(false);
                resetCubeInputsButton.setVisible(false);
                resetCubeInputsButton.setEnabled(false);
                proceedButton.setVisible(false);
                proceedButton.setEnabled(false);
            }
            case COLOR_SELECTION -> {
                if (inSolution) {
                    startButton.setEnabled(true);
                    startButton.setVisible(true);
                    stopButton.setEnabled(true);
                    stopButton.setVisible(true);
                    animSpeedSlider.setEnabled(true);
                    animSpeedSlider.setVisible(true);
                    skipButton.setEnabled(true);
                    skipButton.setVisible(true);
                    rewindButton.setEnabled(true);
                    rewindButton.setVisible(true);

                    randomizeButton.setEnabled(false);
                    randomizeButton.setVisible(false);
                    sideChooser.setVisible(false);
                    sideChooser.setEnabled(false);
                    resetCubeInputsButton.setVisible(false);
                    resetCubeInputsButton.setEnabled(false);
                    proceedButton.setVisible(false);
                    proceedButton.setEnabled(false);
                } else {
                    startButton.setEnabled(false);
                    startButton.setVisible(false);
                    stopButton.setEnabled(false);
                    stopButton.setVisible(false);
                    animSpeedSlider.setEnabled(false);
                    animSpeedSlider.setVisible(false);
                    randomizeButton.setEnabled(false);
                    randomizeButton.setVisible(false);

                    skipButton.setEnabled(false);
                    skipButton.setVisible(false);
                    rewindButton.setEnabled(false);
                    rewindButton.setVisible(false);
                    sideChooser.setVisible(true);
                    sideChooser.setEnabled(true);
                    resetCubeInputsButton.setVisible(true);
                    resetCubeInputsButton.setEnabled(true);
                    proceedButton.setVisible(true);
                    proceedButton.setEnabled(true);
                }
                //Disable all components specific to text scramble mode
                scrambleTextField.setEnabled(false);
                scrambleTextField.setVisible(false);
                applyScrambleButton.setEnabled(false);
                applyScrambleButton.setVisible(false);
            }
        }
    }

    /**
     * Updates the mode to either text scramble or color selection mode based on the parameter.
     *
     * @param mode the mode to change to
     */
    void updateMode(final Mode mode) {
        if (!this.mode.equals(mode)) {
            this.mode = mode;
            cube = new Cube();
            if (this.mode.equals(TEXT_SCRAMBLE)) {
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
        if (mode == Mode.COLOR_SELECTION && !inSolution) {
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
