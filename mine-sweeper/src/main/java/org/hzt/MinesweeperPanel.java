// Author:		Charles Duncan (CharlesETD@gmail.com)
// Compiler:	Javac 1.7.0_02 (Java 1.7.0_60-b19)
// Created:		2/13/15
// Assignment:	1.6
// © Copyright 2015 Charles Duncan
package org.hzt;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class MinesweeperPanel extends JPanel {

    private final MinesweeperGame game;
    private final JLabel mineLabel;
    private final JLabel statusLabel;
    private final JPanel gridPanel;
    private int mineCount;

    /**
     * of tiles per column and row or 0 for default
     * a tile is a mine or -1.0 for default
     * seed to use or -1 for variable seed
     * @param game the minesweeper frame
     */
    public MinesweeperPanel(final MinesweeperGame game) {
        this.game = game;
        gridPanel = new JPanel();
        mineLabel = new JLabel("Mines: " + mineCount, SwingConstants.CENTER);
        statusLabel = new JLabel("Careful!", SwingConstants.CENTER);
        reset();

        setLayout(new BorderLayout());

        add(mineLabel, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);
        add(gridPanel);
    }

    public void reset() {
        gridPanel.removeAll();
        final var sideLength = game.getSideLength();
        final var numberOfTiles = sideLength * sideLength;
        gridPanel.setLayout(new GridLayout(sideLength, sideLength));

        for (var i = 0; i < numberOfTiles; i++) {
            final var tile = new Tile(i, this);
            tile.addMouseListener(new MouseHandler());
            gridPanel.add(tile);
        }
        mineCount = game.getEstimatedNumberOfMines();

        mineLabel.setText("Mines: " + mineCount);
        statusLabel.setText("Careful!");

        repaint();
    }
    /**
     * Returns the instance of the MinesweeperGame.
     *
     * @return Returns game.
     */
    public MinesweeperGame getGame() {
        return game;
    }

    /**
     * Handles button presses for the game.
     */
    private class MouseHandler extends MouseAdapter {

        /**
         * Handles button presses for the game.
         *
         * @param e the MouseEvent to be handled.
         */
        @Override
        public void mousePressed(final MouseEvent e) {
            final var tile = (Tile) (e.getSource());
            if (e.getButton() == MouseEvent.BUTTON3) {
                getGame().flagTile(tile.getTileIndex());
            }
            if (e.getButton() == MouseEvent.BUTTON1) {
                getGame().exploreTile(tile.getTileIndex());
            }
            mineCount = switch (game.getGameState()) {
                case PLAYING -> game.getEstimatedNumberOfMines();
                case WON -> setStatusAndReturnNrOfTiles("Won");
                case GAME_OVER -> setStatusAndReturnNrOfTiles("Lost");
            };
            repaint();
        }

        private int setStatusAndReturnNrOfTiles(final String result) {
            MinesweeperPanel.this.statusLabel.setText("You " + result + " In A Mere " + getGame().getFinalTime() + " Seconds!");
            return getGame().getNumberOfMines();
        }
    }
}
