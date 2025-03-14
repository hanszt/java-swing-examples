// Author:		Charles Duncan (CharlesETD@gmail.com)
// Compiler:	Javac 1.7.0_02 (Java 1.7.0_60-b19)
// Created:		2/13/15
// Assignment:	1.6
// © Copyright 2015 Charles Duncan
package org.hzt;

import org.hzt.utils.numbers.IntX;
import org.hzt.utils.sequences.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class Minesweeper {

    private static final Logger LOGGER = LoggerFactory.getLogger(Minesweeper.class);
    
    record Arguments(int numberOfTiles, double mineProbability, long debugSeed) {
    }

    static Arguments parseArguments(final String... args) {
        LOGGER.debug("Parsing arguments...");
        final var arguments = Sequence.of(args);
        if (!IntX.isEven(args.length) || arguments.any("-help".trim()::equals)) {
            displayHelpMessage();
        }
        final var argumentMap = arguments
                .zipWithNext()
                .toMap();

        return parseArguments(argumentMap);
    }

    private static Arguments parseArguments(final Map<String, String> argumentMap) {
        try {
            final var numberOfTiles = Integer.parseInt(argumentMap.getOrDefault("-numTiles", "0"));
            final var mineProbability  = Double.parseDouble(argumentMap.getOrDefault("-mineProb", "-1.0"));
            final var debugSeed  = Integer.parseInt(argumentMap.getOrDefault("-seed", "-1"));
            LOGGER.info("Arguments parsed successfully.");
            return new Arguments(numberOfTiles, mineProbability, debugSeed);
        } catch (final NumberFormatException e) {
            LOGGER.error("Could not parse arguments: {}", argumentMap, e);
            displayHelpMessage();
            return new Arguments(0, -1.0, -1);
        }
    }

    private static void displayHelpMessage() {
        LOGGER.info("Usage: java Minesweeper -option1 param1 -option2 param2 ...");
        LOGGER.info("Where options include:");
        LOGGER.info("\t-numTiles <integer value>\tThe number of tiles wide and tall");
        LOGGER.info("\t-mineProb <decimal value>\tThe probability of a tile being a mine");
        LOGGER.info("\t-seed <integer value>\t\tThe exact random seed to use");
        LOGGER.info("\t-help\t\t\t\tPrints the help message\n");
    }

    /**
     * Program entry point.
     *
     */
    public static void main(final String... args) {
        LOGGER.info("Starting Minesweeper ...");
        final var arguments = parseArguments(args);
        new MinesweeperFrame(arguments);
    }

}
