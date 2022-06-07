// Author:		Charles Duncan (CharlesETD@gmail.com)
// Compiler:	Javac 1.7.0_02 (Java 1.7.0_60-b19)
// Created:		2/13/15
// Assignment:	1.6
// © Copyright 2015 Charles Duncan
package org.hzt;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public final class MinesweeperFrame extends JFrame {

    private final transient MinesweeperGame game;
    private final transient Runnable panelResetter;
    private final JFileChooser fileBrowser;

    /**
     * @param arguments the parsed arguments or default values
     */
    public MinesweeperFrame(Minesweeper.Arguments arguments) {
        setTitle("Minesweeper");
        setSize(800, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        game = new MinesweeperGame(arguments.numberOfTiles(), arguments.mineProbability(), arguments.debugSeed());

        fileBrowser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Minesweeper Game (.msg)",
                MinesweeperGame.getFileExtension());
        fileBrowser.setFileFilter(filter);
        fileBrowser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        setJMenuBar(buildMenuBar());

        MinesweeperPanel panel = new MinesweeperPanel(this);
        panelResetter = panel::reset;
        add(panel);
        setVisible(true);
    }

    /**
     * Returns the instance of the MinesweeperGame.
     *
     * @return Returns game.
     */
    public MinesweeperGame getGame() {
        return game;

    }

    public void reset() {
        panelResetter.run();
        invalidate();
        validate();
        repaint();
    }

    /**
     * Asks the player where to load and loads the game.
     */
    private void loadGame() {
        int fileChooserReturnValue = fileBrowser.showOpenDialog(this);
        if (fileChooserReturnValue == JFileChooser.APPROVE_OPTION) {
            File saveFile = fileBrowser.getSelectedFile();
            String filename = saveFile.getName();
            if (!filename.contains(".")) {
                saveFile = new File(filename + "." + MinesweeperGame.getFileExtension());
			}
            if (!game.load(saveFile)) {
                JOptionPane.showMessageDialog(this, "Could not load game.");
            }
        }
        reset();
    }

    /**
     * Asks the player where to save and saves the game.
     */
    private void saveGame() {
        int fileChooserReturnValue = fileBrowser.showSaveDialog(this);
        if (fileChooserReturnValue == JFileChooser.APPROVE_OPTION) {

            File saveFile = fileBrowser.getSelectedFile();
            String filename = saveFile.getName();
            if (!filename.contains(".")) {
                saveFile = new File(filename + "." + MinesweeperGame.getFileExtension());
            }
            if (!game.save(saveFile)) {
                JOptionPane.showMessageDialog(this, "Could not save to file.");
            }
        }
        reset();
    }

    /**
     * Asks the player for a difficulty and starts a new game.
     */
    private void newGame() {
        String difficulty = (String) (JOptionPane.showInputDialog(this,
                "Difficulty:",
                "New Game", JOptionPane.QUESTION_MESSAGE,
                null, new String[]{"Easy", "Intermediate", "Expert"}, "Intermediate"));

        if (difficulty != null) {
            switch (difficulty) {
                case "Easy" -> game.newGame(MinesweeperGame.Difficulty.EASY);
                case "Expert" -> game.newGame(MinesweeperGame.Difficulty.EXPERT);
                default -> game.newGame(MinesweeperGame.Difficulty.INTERMEDIATE);
            }
        }
        reset();
    }

    /**
     * Constructs the menu bar and its elements. Menu bar is added after completion.
     *
     * @return the configured menubar
     */
    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenuItem newMenuItem = new JMenuItem("New");
        newMenuItem.addActionListener(e -> newGame());
        menuBar.add(newMenuItem);

        JMenuItem loadMenuItem = new JMenuItem("Load");
        loadMenuItem.addActionListener(e -> loadGame());
        menuBar.add(loadMenuItem);

        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener(e -> saveGame());
        menuBar.add(saveMenuItem);

        JMenuItem quitMenuItem = new JMenuItem("Quit");
        quitMenuItem.addActionListener(e -> System.exit(0));
        menuBar.add(quitMenuItem);

        return menuBar;
    }

}
