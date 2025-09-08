package org.hzt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.hzt.MineSweeperSaver.FILE_EXTENSION;

public final class MineSweeperLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(MineSweeperLoader.class);

    private MineSweeperLoader() {
    }

    /**
     * Loads a game from the given file.
     *
     * @param saveFile to load from.
     * @return the loaded game
     * @throws IllegalStateException if it failed to load
     */
    static MinesweeperGame load(final File saveFile) throws CouldNotLoadGameException {
        LOGGER.info("loading saved file {}", saveFile);
        final var filename = saveFile.getName();

        final var file = filename.contains(".") ? saveFile : new File(filename + "." + FILE_EXTENSION);
        final var name = saveFile.getName();

        if (file.exists() && file.canRead() && (name.substring(name.indexOf("."))).equals("." + FILE_EXTENSION)) {

            try (final var inStream = new FileInputStream(file);
                 final var bufferedInStream = new BufferedInputStream(inStream);
                 final var objectStream = new ObjectInputStream(bufferedInStream)) {
                final var mineProbability = objectStream.readDouble();
                final var randomSeed = objectStream.readLong();
                final var numberOfMines = objectStream.readInt();
                final var sideLength = objectStream.readInt();
                final var numberOfTilesExplored = objectStream.readInt();
                final var startTime = System.currentTimeMillis() - objectStream.readLong();
                final var stopTime = objectStream.readLong();
                final var gameState = (MinesweeperGame.GameState) (objectStream.readObject());
                final var flags = castObjectToList(objectStream.readObject());
                final var gameGrid = (int[]) (objectStream.readObject());
                final var explored = (boolean[]) (objectStream.readObject());
                return new MinesweeperGame(
                        mineProbability,
                        randomSeed,
                        numberOfMines,
                        sideLength,
                        numberOfTilesExplored,
                        startTime,
                        stopTime,
                        gameState,
                        flags,
                        gameGrid,
                        explored
                );
            } catch (final IOException | ClassNotFoundException e) {
                throw new CouldNotLoadGameException("Io exception", e);
            }
        }
        throw new CouldNotLoadGameException("File name was invalid " + filename + " or could not read file");
    }

    @SuppressWarnings("unchecked")
    private static List<Integer> castObjectToList(final Object obj) {
        return obj instanceof final List<?> list ? (List<Integer>) list : emptyList();
    }

    static class CouldNotLoadGameException extends Exception {

        CouldNotLoadGameException(final String message, final Throwable cause) {
            super(message, cause);
        }

        CouldNotLoadGameException(final String message) {
            super(message);
        }
    }

}
