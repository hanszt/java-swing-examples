// Author:		Charles Duncan (CharlesETD@gmail.com)
// Compiler:	Javac 1.7.0_02 (Java 1.7.0_60-b19)
// Created:		2/13/15
// Assignment:	1.6
// © Copyright 2015 Charles Duncan
package org.hzt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Optional;

public final class Tile extends JButton {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tile.class);

	private final int tileIndex;
    private final MinesweeperPanel panel;

    private final ImageIcon mineImage;

    private final ImageIcon flagImage;
    private int state;

    /**
     * Ctor
     *
     * @param tileIndex        index of corresponding tile in gameState
     * @param minesweeperPanel panel that is running the game.
     */
    public Tile(final int tileIndex, final MinesweeperPanel minesweeperPanel) {
        super("");
        this.tileIndex = tileIndex;
        this.panel = minesweeperPanel;
        state = minesweeperPanel.getGame().getStateOf(this.tileIndex);
        setEnabled(true);
        mineImage = getResourceImage("/images/mine.png");
        flagImage = getResourceImage("/images/flag.png");
    }

    private static ImageIcon getResourceImage(final String fileName) {
        final var defaultToolkit = Toolkit.getDefaultToolkit();
        final var filePath = Optional.ofNullable(Tile.class.getResource(fileName))
                .map(URL::getFile)
                .orElseGet(() -> logAndGetEmptyPath(fileName));
        return new ImageIcon(defaultToolkit.getImage(filePath));
    }

    private static String logAndGetEmptyPath(final String fileName) {
        LOGGER.warn("Could not find resource: {}", fileName);
        return "";
    }

    /**
     * Gets this tile's index.
     *
     * @return Returns the tile index.
     */
    public int getTileIndex() {
        return tileIndex;
    }

    /**
     * Draws the graphics of the tile.
     *
     * @param graphics object to draw to
     */
    @Override
    public void paintComponent(final Graphics graphics) {
        super.paintComponent(graphics);
        updateState();
    }

    private void updateState() {
        state = panel.getGame().getStateOf(this.tileIndex);

        switch (state) {
            case 0, 1, 2, 3, 4, 5, 6, 7, 8 -> setNeighborCount(state);
            case MinesweeperGame.MINE -> detonateMine();
            case MinesweeperGame.FLAGGED -> flagSpot();
            case MinesweeperGame.UNEXPLORED -> unexploredTile();
            default -> throw new IllegalStateException("Unsupported state " + state);
        }
    }

    private void unexploredTile() {
        setText("?");
        setIcon(null);
        setDisabledIcon(null);
    }

    private void flagSpot() {
        setText("");
        setIcon(flagImage);
    }

    private void detonateMine() {
        setEnabled(false);
        setText("");
        setIcon(mineImage);
        setDisabledIcon(mineImage);
    }

    private void setNeighborCount(final int neighborCount) {
        // This would be much cleaner if Java supported case statements with range
        // Also setting the colour of the number based on how many mines are near was my plan,
        // but disabling the button greys out the contents, and I don't know how to stop that.
        setEnabled(false);
        setIcon(null);
        setDisabledIcon(null);
        setText(neighborCount == 0 ? " " : Integer.toString(neighborCount));
    }

    @Override
    public String toString() {
        return "Tile{" +
                "tileIndex=" + tileIndex +
                ", state=" + state +
                '}';
    }
}
