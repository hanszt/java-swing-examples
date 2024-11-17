// Author:		Charles Duncan (CharlesETD@gmail.com)
// Compiler:	Javac 1.7.0_02 (Java 1.7.0_60-b19)
// Created:		2/13/15
// Assignment:	1.6
// © Copyright 2015 Charles Duncan
package org.hzt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class MinesweeperGame {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinesweeperGame.class);

    public static final int MINE = -1; // This could be useful for outside classes to know
    public static final int FLAGGED = -2; // This could be useful for outside classes to know
    public static final int UNEXPLORED = -3; // This could be useful for outside classes to know

    private static final int EASY_NUMBER_OF_TILES = 9;
    private static final double EASY_MINE_PROBABILITY = 0.123456789;

    private static final int INTERMEDIATE_NUMBER_OF_TILES = 16;
    private static final double INTERMEDIATE_MINE_PROBABILITY = 0.15625;

    private static final int EXPERT_NUMBER_OF_TILES = 22;
    private static final double EXPERT_MINE_PROBABILITY = 0.20661157;

    public static final String TILE_INVALID = "Tile Invalid";

    final double mineProbability;
    final long randomSeed;
    final int[] gameGrid;
    final List<Integer> flags;
    final boolean[] explored;
    final int numberOfMines;
    final int sideLength;
    final long startTime;

    private int numberOfTilesExplored;
    private long stopTime;

    public enum GameState {PLAYING, GAME_OVER, WON}

    public enum Difficulty {EASY, INTERMEDIATE, EXPERT}

    private GameState gameState;

    /**
     * Ctor
     *
     * @param numberOfTilesPerSide of tiles or 0 for default
     * @param mineProbability      a tile is a mine or -1.0 for default
     * @param debugSeed            seed to use or -1 for variable seed
     */
    public static MinesweeperGame start(
            final int numberOfTilesPerSide,
            final double mineProbability,
            final long debugSeed
    ) {
        final var sideLength = numberOfTilesPerSide < 1 ? INTERMEDIATE_NUMBER_OF_TILES : numberOfTilesPerSide;
        final var numberOfTiles = sideLength * sideLength;
        return new MinesweeperGame(
                mineProbability,
                debugSeed,
                -1,
                sideLength,
                0,
                System.currentTimeMillis(),
                -1L,
                GameState.PLAYING,
                new ArrayList<>(),
                new int[numberOfTiles],
                new boolean[numberOfTiles]
        );
    }

    public MinesweeperGame(
            double mineProbability,
            long debugSeed,
            int numberOfMines,
            int sideLength,
            int numberOfTilesExplored,
            long startTime,
            long stopTime,
            GameState gameState,
            List<Integer> flags,
            int[] gameGrid,
            boolean[] explored
    ) {
        this.stopTime = stopTime;
        this.numberOfTilesExplored = numberOfTilesExplored;
        this.gameState = gameState;
        this.sideLength = sideLength;
        this.mineProbability = mineProbability < 0.0 ? INTERMEDIATE_MINE_PROBABILITY : mineProbability;
        this.randomSeed = debugSeed == -1 ? System.currentTimeMillis() : debugSeed;
        this.gameGrid = gameGrid;
        this.explored = explored;
        this.numberOfMines = numberOfMines == -1 ? propagateGameGrid() : numberOfMines;
        this.flags = flags;
        this.startTime = startTime;
    }

    /**
     * Gets the current gameState
     *
     * @return Returns the gameState
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Gets the number of mines on the field
     *
     * @return Returns the number of mines on the field
     */
    public int getNumberOfMines() {
        return numberOfMines;
    }

    /**
     * Gets the square length of the field
     *
     * @return Returns squareLength
     */
    public int getSideLength() {
        return sideLength;
    }

    /**
     * Gets the number of mines - number of flags.
     *
     * @return Returns the number of mines - number of flags.
     */
    public int getEstimatedNumberOfMines() {
        return numberOfMines - flags.size();
    }

    /**
     * Gets the number of mines adjacent to a tile or MINE if the tile is a mine or FLAGGED if the tile is flagged
     * or UNEXPLORED if the tile has not been explored yet.
     *
     * @param position of the tile to check.
     * @return Returns the number of mines adjacent to a tile or MINE, FLAGGED, or UNEXPLORED.
     */
    public int getStateOf(final int position) {
        // Is the position given invalid?
        if (position < 0 || position >= gameGrid.length) {
            LOGGER.error(TILE_INVALID);
            assert (false);
        }
        if (explored[position]) {
            return gameGrid[position];
        }
        for (final int flag : flags) {
            if (flag == position) {
                return FLAGGED;
            }
        }
        return UNEXPLORED;
    }

    /**
     * Gets the time elapsed since the game started to when it ended.
     *
     * @return Returns the time elapsed since the game started to when it ended in seconds.
     */
    public float getFinalTime() {
        return (stopTime - startTime) / 1000.0F;
    }

    /**
     * Converts the gameboard to a printable string
     */
    public String boardAsString() {
        final var stringBuilder = new StringBuilder();
        for (var i = 0; i < gameGrid.length; i++) {
            if (i != 0 && i % sideLength == 0) {
                stringBuilder.append(System.lineSeparator());
            }
            stringBuilder.append(gameGrid[i] == MINE ? "*" : String.valueOf(gameGrid[i]));
        }
        return stringBuilder.toString();
    }

    /**
     * Checks to see if the selected tile is a mine or not, alters gameState
     *
     * @param position of tile to check
     */
    public void exploreTile(final int position) {
        // Is the position given invalid?
        if (position < 0 || position >= gameGrid.length) {
            LOGGER.error(TILE_INVALID);
            assert (false);
        }
        if (explored[position]) {
            return;
        }
        explored[position] = true;

        if (gameGrid[position] != MINE) {
            numberOfTilesExplored++;
            if (numberOfMines + numberOfTilesExplored == gameGrid.length) {
                gameState = GameState.WON;
                stopTime = System.currentTimeMillis();
                revealAll();
                return;
            }
            if (gameGrid[position] == 0) {
                exploreAdjacent(position);
            }
            return;
        }

        gameState = GameState.GAME_OVER;
        stopTime = System.currentTimeMillis();
        revealAll();

    }

    /**
     * Toggles a tile to be flagged or unflagged.
     *
     * @param position of tile to flagged
     */
    public void flagTile(final int position) {
        // Is the position given invalid?
        if (position < 0 || position >= gameGrid.length) {
            LOGGER.error(TILE_INVALID);
            assert (false);
        }

        if (explored[position]) {
            return;
        }

        for (var i = 0; i < flags.size(); i++) {
            final var flag = flags.get(i);
            if (flag == position) {
                flags.remove(flag);
                return;
            }
        }
        if (flags.size() < numberOfMines) {
            flags.add(position);
        }
    }

    /**
     * Creates a new game with the given difficulty.
     *
     * @param difficulty of the game
     */
    public MinesweeperGame newGame(final Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> MinesweeperGame.start(EASY_NUMBER_OF_TILES, EASY_MINE_PROBABILITY, -1);
            case INTERMEDIATE -> MinesweeperGame.start(INTERMEDIATE_NUMBER_OF_TILES, INTERMEDIATE_MINE_PROBABILITY, -1);
            case EXPERT -> MinesweeperGame.start(EXPERT_NUMBER_OF_TILES, EXPERT_MINE_PROBABILITY, -1);
        };
    }

    /**
     * Fills the game grid with a random number of mines and calculates the adjacent mines for each tile
     */
    private int propagateGameGrid() {
        final var random = new Random(randomSeed);

        var numberOfMines = 0;
        for (var i = 0; i < gameGrid.length; i++) {
            if (random.nextDouble() <= mineProbability) {
                gameGrid[i] = MINE;
                numberOfMines++;
                updateAdjacent(i);
            }
        }
        return numberOfMines;
    }

    /**
     * Increases the adjacent mine count of nearby tiles
     *
     * @param minePosition of mine to update around
     */
    private void updateAdjacent(final int minePosition) {
        // Is the position given invalid?
        if (minePosition < 0 || minePosition >= gameGrid.length || gameGrid[minePosition] != MINE) {
            return;
        }
        // Adjust adjacent mine count of nearby tiles
        for (var j = -1; j <= 1; j++) {
            for (var k = -1; k <= 1; k++) {
                adjustMineCountNearbyTile(minePosition, j, k);
            }
        }
    }

    private void adjustMineCountNearbyTile(final int minePosition, final int j, final int k) {
        var adjacentIndex = minePosition + (j * sideLength);
        // Avoid counting first and last tiles as adjacent when mine is a first or last element
        if (adjNotLast(k, adjacentIndex) && adjNotFirst(k, adjacentIndex)) {
            adjacentIndex += k;

            // These conditions must be checked twice to avoid catching the end of a line when k = 1
            if (adjacentIndex >= 0 && adjacentIndex < gameGrid.length && gameGrid[adjacentIndex] != MINE) {
                gameGrid[adjacentIndex]++;
            }
        }
    }

    /**
     * Explores all adjacent tiles.
     *
     * @param position of tile to update around
     */
    private void exploreAdjacent(final int position) {
        // Is the position given invalid?
        if (position < 0 || position >= gameGrid.length) {
            return;
        }
        // Adjust adjacent mine count of nearby tiles
        for (var j = -1; j <= 1; j++) {
            for (var k = -1; k <= 1; k++) {
                exploreTile(position, j, k);
            }
        }
    }

    private void exploreTile(final int position, final int j, final int k) {
        var adjacentIndex = position + (j * sideLength);
        // Avoid counting first and last tiles as adjacent when mine is a first or last element
        if (adjNotFirst(k, adjacentIndex) && adjNotLast(k, adjacentIndex)) {
            adjacentIndex += k;

            // These conditions must be checked twice to avoid catching the end of a line when k = 1
            if (adjacentIndex >= 0 && adjacentIndex < gameGrid.length) {
                exploreTile(adjacentIndex);
            }
        }
    }

    private boolean adjNotLast(final int k, final int adjacentIndex) {
        return adjacentIndex % sideLength != 0 || k != -1;
    }

    private boolean adjNotFirst(final int k, final int adjacentIndex) {
        return (adjacentIndex + 1) % sideLength != 0 || k != 1;
    }

    /**
     * Explores all tiles when the game is over or won.
     */
    private void revealAll() {
        Arrays.fill(explored, true);
    }

    public long getStopTime() {
        return stopTime;
    }

    public int getNumberOfTilesExplored() {
        return numberOfTilesExplored;
    }
}
