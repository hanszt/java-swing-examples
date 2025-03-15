package org.hzt;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * Copyright 2017, Shoumyo Chakravorti, All rights reserved.
 * <p>
 * Licensed under the MIT License.
 * <p>
 * The CubeDisplayer class extends JFrame, allowing for the display of a CubePainter JPanel which
 * the user can interact with. The CubeDisplayer class allows for toggling between solution modes.
 *
 * @author Shoumyo Chakravorti
 * @version 2.0
 */
public final class CubeDisplayer extends JFrame {
    //Auto-generated ID
    private static final long serialVersionUID = -3198702237161500498L;
    CubePainter cubePainter; //The JPanel that will handle painting and user input
    JMenuBar menuBar;
    JMenu modes;
    JMenuItem colorSelection, scramble;
    //JMenuItem colorSelection, scramble;

    public static void main(final String[] args) {
        EventQueue.invokeLater(CubeDisplayer::new);
    }


    /**
     * Creates a new CubeDisplayer and initializes it with a new CubePainter for the user
     * to interact with.
     */
    public CubeDisplayer() {
        setTitle("Cube Displayer");
        setLayout(new BorderLayout());
        setSize(900, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setIgnoreRepaint(true);

        menuBar = new JMenuBar();
        modes = new JMenu("Mode Selection");
        colorSelection = new JMenuItem("Color Selection Mode");
        scramble = new JMenuItem("Text Scramble Mode");
        modes.add(colorSelection);
        modes.add(scramble);
        colorSelection.addActionListener(unused -> {
            cubePainter.setInSolution(false);
            cubePainter.updateMode(CubePainter.COLOR_SELECTION);
        });
        scramble.addActionListener(unused -> {
            cubePainter.setInSolution(true);
            cubePainter.updateMode(CubePainter.TEXT_SCRAMBLE);
        });
        menuBar.add(modes);
        setJMenuBar(menuBar);

        //Create a new CubePainter JPanel
        cubePainter = new CubePainter(new Random());
        add(cubePainter);
        cubePainter.setVisible(true);
        cubePainter.setEnabled(true);

        menuBar.setVisible(true);
        setVisible(true);
        this.repaint();
    }
}

