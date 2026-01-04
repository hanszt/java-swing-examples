package org.hzt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright 2017, Shoumyo Chakravorti, All rights reserved.
 * <p>
 * Licensed under the MIT License.
 * <p>
 * The SolutionFinder class outputs the solution to a given scramble on a cube to the console.
 * This class was heavily used prior to development of the GUI.
 * 
 * @author Shoumyo Chakravorti
 * @version 2.0
 */
public final class SolutionFinder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SolutionFinder.class);

	void main() {
		double sum = 0;
		
		final var startTime = System.nanoTime();
		LOGGER.info("Initializing...");
		final var cube = new Cube();
		//Scramble it up
		final var scramble = "L R' U2 B2 L2 U2 B2 L D2 R' F2 D B F2 L' D U2 B R' U' L'";
		LOGGER.info("Scramble: {}\n", scramble);
		cube.scramble(scramble);
		
		final var sunflower = cube.makeSunflower();
		LOGGER.info("Making the sunflower:");
		LOGGER.info(sunflower);
        LOGGER.atInfo().setMessage(() -> ("Optimized: \n" + cube.optimizeMoves(sunflower) + "\n")).log();
		
		final var whiteCross = cube.makeWhiteCross();
		LOGGER.info("Making the white cross:");
		LOGGER.info(whiteCross);
		LOGGER.atInfo().setMessage(() -> ("Optimized: \n" + cube.optimizeMoves(whiteCross) + "\n")).log();
		
		final var whiteCorners = cube.finishWhiteLayer();
		LOGGER.info("Inserting the white corners:");
		LOGGER.info(whiteCorners);
		LOGGER.atInfo().setMessage(() -> ("Optimized: \n" + cube.optimizeMoves(whiteCorners) + "\n")).log();
		
		final var edges = cube.insertAllEdges();
		LOGGER.info("Finishing second layer:");
		LOGGER.info(edges);
		LOGGER.atInfo().setMessage(() -> ("Optimized: \n" + cube.optimizeMoves(edges) + "\n")).log();
		
		final var yellowCross = cube.makeYellowCross();
		LOGGER.info("Making the yellow cross:");
		LOGGER.info(yellowCross);
		LOGGER.atInfo().setMessage(() -> ("Optimized: \n" + cube.optimizeMoves(yellowCross) + "\n")).log();
		
		final var OLL = cube.orientLastLayer();
		LOGGER.info("Orienting the last layer:");
		LOGGER.info(OLL);
		LOGGER.atInfo().setMessage(() -> ("Optimized: \n" + cube.optimizeMoves(OLL) + "\n")).log();
		
		final var PLL = cube.permuteLastLayer();
		LOGGER.info("Permuting the last layer:");
		LOGGER.info(PLL);
		LOGGER.atInfo().setMessage(() -> ("Optimized: \n" + cube.optimizeMoves(PLL) + "\n")).log();
		
		final var endTime = System.nanoTime();
		final var runtime = endTime - startTime;
		sum+=runtime;
        LOGGER.info("Done in {} milliseconds\n\n\n\n", sum / 1000000);
	}
}



