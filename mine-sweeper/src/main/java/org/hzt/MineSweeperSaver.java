package org.hzt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public final class MineSweeperSaver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MineSweeperSaver.class);

    static final String FILE_EXTENSION = "msg";

    private MineSweeperSaver() {
    }

    /**
     * Saves a game to the given file.
     *
     * @param saveFile to save to.
     * @return false if failed to save.
     */
    public static boolean save(final MinesweeperGame minesweeperGame, final File saveFile) {

        final var filename = saveFile.getName();

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

        final var file = filename.contains(".") ? saveFile : new File(filename + "." + FILE_EXTENSION);

        final var name = file.getName();
        if (name.substring(name.indexOf(".")).equals("." + FILE_EXTENSION)) {

            try (final var outStream = new FileOutputStream(file);
                 final var bufferedOutStream = new BufferedOutputStream(outStream);
                 final var objectStream = new ObjectOutputStream(bufferedOutStream)) {
                objectStream.writeDouble(minesweeperGame.mineProbability);
                objectStream.writeLong(minesweeperGame.randomSeed);
                objectStream.writeInt(minesweeperGame.numberOfMines);
                objectStream.writeInt(minesweeperGame.sideLength);
                objectStream.writeInt(minesweeperGame.getNumberOfTilesExplored());
                objectStream.writeLong(System.currentTimeMillis() - minesweeperGame.startTime);
                objectStream.writeLong(minesweeperGame.getStopTime());
                objectStream.writeObject(minesweeperGame.getGameState());
                objectStream.writeObject(minesweeperGame.flags);
                objectStream.writeObject(minesweeperGame.gameGrid);
                objectStream.writeObject(minesweeperGame.explored);

                bufferedOutStream.flush();
            } catch (final IOException e) {
                LOGGER.error("Exception while saving", e);
                return false;
            }
            return true;
        }
        return false;
    }

}
