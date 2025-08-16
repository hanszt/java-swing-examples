package org.hzt.ant;/*
 * org.hzt.ant.Game class - main method runs the org.hzt.ant.Ant Simulation game
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.EventQueue;// imports library needed to use EventQueue
import javax.swing.*;

public final class Game extends JFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(Game.class);
	private static final long serialVersionUID = -7803629994015778818L;// serialization ID

	// org.hzt.ant.Game() constructor - no parameters necessary, creates instance of org.hzt.ant.Ant Simulation game
	public Game() {
		setContentPane(new Board());// sets the content pane to that of an instance of the org.hzt.ant.Board class
		pack();// packs the contents into the JFrame
		setResizable(false);// does not allow the game to be resized
		
		setTitle("org.hzt.ant.Ant Simulation");// sets title on window to "org.hzt.ant.Ant Simulation"
		setLocationRelativeTo(null);// places window is center of the screen
		JFrame.setDefaultLookAndFeelDecorated(true);// uses Windows decorations
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);// exits the application when the window is closed
        LOGGER.info("Ant game started");
	}// org.hzt.ant.Game() constructor
	
	// main method - used to run the org.hzt.ant.Ant Simulation game
	public static void main(String[] args) {
        LOGGER.info("Starting ant game...");
        // run() method
        EventQueue.invokeLater(() -> {
            Game game = new Game();// creates new org.hzt.ant.Game instance
            game.setVisible(true);// sets the game instance to be visible
        });// EventQueue.invokeLater(Runnable())
	}// main(String[]) method
}// org.hzt.ant.Game class
