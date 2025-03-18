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

        final var menuBar = new JMenuBar();
        final var modes = new JMenu("Mode Selection");
        final var colorSelection = new JMenuItem("Color Selection Mode");
        final var scramble = new JMenuItem("Text Scramble Mode");
        modes.add(colorSelection);
        modes.add(scramble);
        //Create a new CubePainter JPanel
        final var cubePainter = new CubePainter(new Random());
        colorSelection.addActionListener(unused -> {
            cubePainter.setInSolution(false);
            cubePainter.updateMode(CubePainter.Mode.COLOR_SELECTION);
        });
        scramble.addActionListener(unused -> {
            cubePainter.setInSolution(true);
            cubePainter.updateMode(CubePainter.Mode.TEXT_SCRAMBLE);
        });
        menuBar.add(modes);
        setJMenuBar(menuBar);

        add(cubePainter);
        cubePainter.setVisible(true);
        cubePainter.setEnabled(true);

        menuBar.setVisible(true);
        setVisible(true);
        this.repaint();
    }
}

