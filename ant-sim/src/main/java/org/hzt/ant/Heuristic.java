package org.hzt.ant;

public final class Heuristic {
	
    public int estimate(int node) {
		// return length(new int[]{node % 40, node / 40}, new int[]{this.goalNode % 16, this.goalNode / 16});// Euclidean heuristic
		return 0;// uses 0 so that A* finds the exact minimum path
	}
}
