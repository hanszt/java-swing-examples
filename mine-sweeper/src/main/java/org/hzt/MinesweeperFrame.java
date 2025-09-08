// Author:		Charles Duncan (CharlesETD@gmail.com)
// Compiler:	Javac 1.7.0_02 (Java 1.7.0_60-b19)
// Created:		2/13/15
// Assignment:	1.6
// © Copyright 2015 Charles Duncan
package org.hzt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

import static org.hzt.MineSweeperSaver.FILE_EXTENSION;

public final class MinesweeperFrame extends JFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinesweeperFrame.class);

    private final JFileChooser fileBrowser;
    private MinesweeperPanel panel;

    /**
     * @param arguments the parsed arguments or default values
     */
    MinesweeperFrame(final Minesweeper.Arguments arguments) {
        setTitle("Minesweeper");
        setSize(800, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        final var game = MinesweeperGame.start(arguments.numberOfTiles(), arguments.mineProbability(), arguments.debugSeed());

        fileBrowser = new JFileChooser();
        final var filter = new FileNameExtensionFilter("Minesweeper Game (.msg)",
                FILE_EXTENSION);
        fileBrowser.setFileFilter(filter);
        fileBrowser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        setJMenuBar(buildMenuBar());

        panel = new MinesweeperPanel(game);
        add(panel);
        setVisible(true);
    }

    private void reset() {
        invalidate();
        validate();
        repaint();
    }

    private void reload(final MinesweeperGame game) {
        remove(panel);
        panel = new MinesweeperPanel(game);
        add(panel);
        reset();
    }

    /**
     * Asks the player where to load and loads the game.
     */
    private void loadGame() {
        final var fileChooserReturnValue = fileBrowser.showOpenDialog(this);
        if (fileChooserReturnValue == JFileChooser.APPROVE_OPTION) {
            final var saveFile = fileBrowser.getSelectedFile();
            final var filename = saveFile.getName();
            try {
                final var file = filename.contains(".") ? saveFile : new File(filename + "." + FILE_EXTENSION);
                reload(MineSweeperLoader.load(file));
                return;
            } catch (final MineSweeperLoader.CouldNotLoadGameException e) {
                final var message = "Could not load game.";
                LOGGER.error(message);
                JOptionPane.showMessageDialog(this, message);
            }
        }
        newGame();
    }

    /**
     * Asks the player where to save and saves the game.
     */
    private void saveGame() {
        final var fileChooserReturnValue = fileBrowser.showSaveDialog(this);
        if (fileChooserReturnValue == JFileChooser.APPROVE_OPTION) {

            var saveFile = fileBrowser.getSelectedFile();
            final var filename = saveFile.getName();
            if (!filename.contains(".")) {
                saveFile = new File(filename + "." + FILE_EXTENSION);
            }
            if (!MineSweeperSaver.save(panel.getGame(), saveFile)) {
                JOptionPane.showMessageDialog(this, "Could not save to file.");
            }
        }
        reset();
    }

    /**
     * Asks the player for a difficulty and starts a new game.
     */
    private void newGame() {
        final var difficulty = (String) (JOptionPane.showInputDialog(this,
                "Difficulty:",
                "New Game", JOptionPane.QUESTION_MESSAGE,
                null, new String[]{"Easy", "Intermediate", "Expert"}, "Intermediate"));

        final var game = panel.getGame();
        final var newGame = switch (difficulty) {
            case "Easy" -> game.newGame(MinesweeperGame.Difficulty.EASY);
            case "Expert" -> game.newGame(MinesweeperGame.Difficulty.EXPERT);
            default -> game.newGame(MinesweeperGame.Difficulty.INTERMEDIATE);
        };
        reload(newGame);
    }

    /**
     * Constructs the menu bar and its elements. Menu bar is added after completion.
     *
     * @return the configured menubar
     */
    private JMenuBar buildMenuBar() {
        final var menuBar = new JMenuBar();

        final var newMenuItem = new JMenuItem("New");
        newMenuItem.addActionListener(unused -> newGame());
        menuBar.add(newMenuItem);

        final var loadMenuItem = new JMenuItem("Load");
        loadMenuItem.addActionListener(unused -> loadGame());
        menuBar.add(loadMenuItem);

        final var saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener(unused -> saveGame());
        menuBar.add(saveMenuItem);

        final var quitMenuItem = new JMenuItem("Quit");
        quitMenuItem.addActionListener(unused -> System.exit(0));
        menuBar.add(quitMenuItem);

        return menuBar;
    }

}
