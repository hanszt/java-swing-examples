// Author:		Charles Duncan (CharlesETD@gmail.com)
// Compiler:	Javac 1.7.0_02 (Java 1.7.0_60-b19)
// Created:		2/13/15
// Assignment:	1.6
// © Copyright 2015 Charles Duncan
package org.hzt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private static final String FILE_EXTENSION = "msg";
    public static final String TILE_INVALID = "Tile Invalid";
    private double mineProbability;
    private long randomSeed;
    private int[] gameGrid;
    private List<Integer> flags;
    private boolean[] explored;
    private int numberOfMines;
    private int sideLength;
    private int numberOfTilesExplored;
    private long startTime;
    private long stopTime;

    public enum GameState {PLAYING, GAME_OVER, WON}

    public enum Difficulty {EASY, INTERMEDIATE, EXPERT}

    private GameState gameState;

    /**
     * Ctor
     *
     * @param numberOfTiles      of tiles or 0 for default
     * @param mineProbability a tile is a mine or -1.0 for default
     * @param debugSeed      seed to use or -1 for variable seed
     */
    public MinesweeperGame(int numberOfTiles, double mineProbability, long debugSeed) {
        reset(numberOfTiles, mineProbability, debugSeed);
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
    public int getStateOf(int position) {
        // Is the position given invalid?
        if (position < 0 || position >= gameGrid.length) {
            LOGGER.error(TILE_INVALID);
            assert (false);
        }
        if (explored[position]) {
            return gameGrid[position];
        }
        for (int flag : flags) {
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
     * Gets the file extension used to save games.
     *
     * @return Returns FILE_EXTENSION.
     */
    public static String getFileExtension() {
        return FILE_EXTENSION;
    }

    /**
     * Converts the gameboard to a printable string
     */
    public String boardAsString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < gameGrid.length; i++) {
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
    public void exploreTile(int position) {
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
    public void flagTile(int position) {
        // Is the position given invalid?
        if (position < 0 || position >= gameGrid.length) {
            LOGGER.error(TILE_INVALID);
            assert (false);
        }

        if (explored[position]) {
            return;
        }

        for (int i = 0; i < flags.size(); i++) {
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
    public void newGame(Difficulty difficulty) {
        switch (difficulty) {
            case EASY -> reset(EASY_NUMBER_OF_TILES, EASY_MINE_PROBABILITY, -1);
            case INTERMEDIATE -> reset(INTERMEDIATE_NUMBER_OF_TILES, INTERMEDIATE_MINE_PROBABILITY, -1);
            case EXPERT -> reset(EXPERT_NUMBER_OF_TILES, EXPERT_MINE_PROBABILITY, -1);
        }
    }

    /**
     * Loads a game from the given file.
     *
     * @param saveFile to load from.
     * @return false if failed to load.
     */
    public boolean load(File saveFile) {
        String filename = saveFile.getName();

        if (!filename.contains(".")) {
            saveFile = new File(filename + "." + FILE_EXTENSION);
            filename = saveFile.getName();
        }
        if (saveFile.exists() && saveFile.canRead() && (filename.substring(filename.indexOf("."))).equals("." + FILE_EXTENSION)) {

            try (FileInputStream inStream = new FileInputStream(saveFile);
                 BufferedInputStream bufferedInStream = new BufferedInputStream(inStream);
                 ObjectInputStream objectStream = new ObjectInputStream(bufferedInStream)) {

                mineProbability = (Double) (objectStream.readObject());
                randomSeed = (Long) (objectStream.readObject());
                numberOfMines = (Integer) (objectStream.readObject());
                sideLength = (Integer) (objectStream.readObject());
                numberOfTilesExplored = (Integer) (objectStream.readObject());
                startTime = System.currentTimeMillis() - (Long) (objectStream.readObject());
                stopTime = (Long) (objectStream.readObject());
                gameState = (GameState) (objectStream.readObject());
                flags = castObjectToList(objectStream.readObject());
                gameGrid = (int[]) (objectStream.readObject());
                explored = (boolean[]) (objectStream.readObject());

            } catch (IOException | ClassNotFoundException e) {
                LOGGER.error("Io exception", e);
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Saves a game to the given file.
     *
     * @param saveFile to save to.
     * @return false if failed to save.
     */
    public boolean save(File saveFile) {

        String filename = saveFile.getName();
	
		/*
		mineProbability
		randomSeed
		numberOfMines
		squareLength
		numberOfTilesExplored
		startTime
		stopTime
		gameState
		flags
		gameGrid
		explored
		*/

        if (!filename.contains(".")) {

            saveFile = new File(filename + "." + FILE_EXTENSION);
            filename = saveFile.getName();

        }

        if (filename.substring(filename.indexOf(".")).equals("." + FILE_EXTENSION)) {

            try (FileOutputStream outStream = new FileOutputStream(saveFile);
                 BufferedOutputStream bufferedOutStream = new BufferedOutputStream(outStream);
                 ObjectOutputStream objectStream = new ObjectOutputStream(bufferedOutStream)) {
                objectStream.writeObject(mineProbability);
                objectStream.writeObject(randomSeed);
                objectStream.writeObject(numberOfMines);
                objectStream.writeObject(sideLength);
                objectStream.writeObject(numberOfTilesExplored);
                objectStream.writeObject(System.currentTimeMillis() - startTime);
                objectStream.writeObject(stopTime);
                objectStream.writeObject(gameState);
                objectStream.writeObject(flags);
                objectStream.writeObject(gameGrid);
                objectStream.writeObject(explored);

                bufferedOutStream.flush();
            } catch (IOException e) {
                LOGGER.error("Exception while saving", e);
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * newGame
     *
     * @param numberOfTilesPerSide      of tiles or 0 for default
     * @param mineProbability a tile is a mine or -1.0 for default
     * @param debugSeed      seed to use or -1 for variable seed
     */
    public void reset(int numberOfTilesPerSide, double mineProbability, long debugSeed) {

        numberOfMines = 0;
        numberOfTilesExplored = 0;
        gameState = GameState.PLAYING;
        stopTime = -1;
        startTime = 0;

        this.sideLength = numberOfTilesPerSide < 1 ? INTERMEDIATE_NUMBER_OF_TILES : numberOfTilesPerSide;
        this.mineProbability = mineProbability < 0.0 ? INTERMEDIATE_MINE_PROBABILITY : mineProbability;
        this.randomSeed = debugSeed == -1 ? System.currentTimeMillis() : debugSeed;

        final int numberOfTiles = sideLength * sideLength;

        gameGrid = new int[numberOfTiles];
        explored = new boolean[numberOfTiles];
        propagateGameGrid();

        flags = new ArrayList<>(numberOfMines);

        startTime = System.currentTimeMillis();

    }

    /**
     * Fills the game grid with a random number of mines and calculates the adjacent mines for each tile
     */
    private void propagateGameGrid() {

        Random random = new Random(randomSeed);

        for (int i = 0; i < gameGrid.length; i++) {

            if (random.nextDouble() <= mineProbability) {

                gameGrid[i] = MINE;
                numberOfMines++;

                updateAdjacent(i);

            }

        }

    }

    /**
     * Increases the adjacent mine count of nearby tiles
     *
     * @param minePosition of mine to update around
     */
    private void updateAdjacent(int minePosition) {
        // Is the position given invalid?
        if (minePosition < 0 || minePosition >= gameGrid.length || gameGrid[minePosition] != MINE) {
            return;
        }
        // Adjust adjacent mine count of nearby tiles
        for (int j = -1; j <= 1; j++) {
            for (int k = -1; k <= 1; k++) {
                adjustMineCountNearbyTile(minePosition, j, k);
            }
        }
    }

    private void adjustMineCountNearbyTile(int minePosition, int j, int k) {
        int adjacentIndex = minePosition + (j * sideLength);
        // Avoid counting first and last tiles as adjacent when mine is a first or last element
        if (adjNotLast(k, adjacentIndex) && adjNotFirst(k, adjacentIndex)) {
            adjacentIndex += k;

            // These conditions must be checked twice to avoid catching the end of a line when k = 1
            if (adjacentIndex >= 0 && adjacentIndex < gameGrid.length && gameGrid[adjacentIndex] != MINE) {
                gameGrid[adjacentIndex]++;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Integer> castObjectToList(Object obj) {
        return obj instanceof List<?> list ? (List<Integer>) list : Collections.emptyList();
    }

    /**
     * Explores all adjacent tiles.
     *
     * @param position of tile to update around
     */
    private void exploreAdjacent(int position) {
        // Is the position given invalid?
        if (position < 0 || position >= gameGrid.length) {
            return;
        }
        // Adjust adjacent mine count of nearby tiles
        for (int j = -1; j <= 1; j++) {
            for (int k = -1; k <= 1; k++) {
                exploreTile(position, j, k);
            }
        }
    }

    private void exploreTile(int position, int j, int k) {
        int adjacentIndex = position + (j * sideLength);
        // Avoid counting first and last tiles as adjacent when mine is a first or last element
        if (adjNotFirst(k, adjacentIndex) && adjNotLast(k, adjacentIndex)) {
            adjacentIndex += k;

            // These conditions must be checked twice to avoid catching the end of a line when k = 1
            if (adjacentIndex >= 0 && adjacentIndex < gameGrid.length) {
                exploreTile(adjacentIndex);
            }
        }
    }

    private boolean adjNotLast(int k, int adjacentIndex) {
        return adjacentIndex % sideLength != 0 || k != -1;
    }

    private boolean adjNotFirst(int k, int adjacentIndex) {
        return (adjacentIndex + 1) % sideLength != 0 || k != 1;
    }

    /**
     * Explores all tiles when the game is over or won.
     */
    private void revealAll() {
        Arrays.fill(explored, true);
    }

}
