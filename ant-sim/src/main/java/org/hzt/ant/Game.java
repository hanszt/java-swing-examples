package org.hzt.ant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;
import java.util.random.RandomGenerator;

/**
 * The Game class - the main class for the Ant Simulation game.
 */
public final class Game extends JFrame {

    private static final RandomGenerator random = RandomGenerator.getDefault();
    private static final Logger LOGGER = LoggerFactory.getLogger(Game.class);
    @Serial
    private static final long serialVersionUID = -7803629994015778818L;

    public Game() {
        setContentPane(new Board(random));
        pack();
        setResizable(false);

        setTitle("org.hzt.ant.Ant Simulation");
        setLocationRelativeTo(null);
        JFrame.setDefaultLookAndFeelDecorated(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        LOGGER.info("Ant game started");
    }

    public static void main(final String[] args) {
        LOGGER.info("Starting ant game...");

        EventQueue.invokeLater(() -> {
            final var game = new Game();
            game.setVisible(true);
        });
    }
}
