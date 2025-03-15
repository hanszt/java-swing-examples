package org.hzt;

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
public class SolutionFinder {

	private static Cube cube;
	
	public static void main(final String[] args) {
		double sum = 0;
		
		final long startTime = System.nanoTime();
		System.out.println("Initializing...");
		cube = new Cube();
		//Scramble it up
		final String scramble = "L R' U2 B2 L2 U2 B2 L D2 R' F2 D B F2 L' D U2 B R' U' L'";
		System.out.println("Scramble: " + scramble + "\n");
		cube.scramble(scramble);
		
		final String sunflower = cube.makeSunflower();
		System.out.println("Making the sunflower:");
		System.out.println(sunflower);
		System.out.println("Optimized: \n" + cube.optimizeMoves(sunflower) + "\n");
		
		final String whiteCross = cube.makeWhiteCross();
		System.out.println("Making the white cross:");
		System.out.println(whiteCross);
		System.out.println("Optimized: \n" + cube.optimizeMoves(whiteCross) + "\n");
		
		final String whiteCorners = cube.finishWhiteLayer();
		System.out.println("Inserting the white corners:");
		System.out.println(whiteCorners);
		System.out.println("Optimized: \n" + cube.optimizeMoves(whiteCorners) + "\n");
		
		final String edges = cube.insertAllEdges();
		System.out.println("Finishing second layer:");
		System.out.println(edges);
		System.out.println("Optimized: \n" + cube.optimizeMoves(edges) + "\n");
		
		final String yellowCross = cube.makeYellowCross();
		System.out.println("Making the yellow cross:");
		System.out.println(yellowCross);
		System.out.println("Optimized: \n" + cube.optimizeMoves(yellowCross) + "\n");
		
		final String OLL = cube.orientLastLayer();
		System.out.println("Orienting the last layer:");
		System.out.println(OLL);
		System.out.println("Optimized: \n" + cube.optimizeMoves(OLL) + "\n");
		
		final String PLL = cube.permuteLastLayer();
		System.out.println("Permuting the last layer:");
		System.out.println(PLL);
		System.out.println("Optimized: " + cube.optimizeMoves(PLL) + "\n");
		
		final long endTime = System.nanoTime();
		final long runtime = endTime - startTime;
		sum+=runtime;
		System.out.println("Done in " + (sum/1000000) + " milliseconds" + "\n\n\n\n");
		
		System.out.println("Here" + "Hello".substring(5));
		
	}
}



