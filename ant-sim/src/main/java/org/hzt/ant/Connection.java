package org.hzt.ant;/*
 * org.hzt.ant.Connection class - used in the org.hzt.ant.Graph class, specifies all the edges in the org.hzt.ant.Graph.
 */

public class Connection {
	private int fromNode;// represents the node that the connection originates at
	private int toNode;// represents the node that the connection ends at
	private int cost;// represents the cost of using the connection
	
	// org.hzt.ant.Connection constructor - originating node, endding node, cost of connection
	public Connection(int fromNode, int toNode, int cost) {
		this.fromNode = fromNode;// sets the originating node to that provided
		this.toNode = toNode;// sets the ending node to that provided
		this.cost = cost;// sets the cost of the connection to that provided
	}// org.hzt.ant.Connection(int, int, int) constructor
	
	// getCost method - allows the cost of the connection to be accessed
	public int getCost() {
		return this.cost;// returns the cost of the connection
	}// getCost() method
	
	// setCost method - allows the cost of the connection to be set to a specified value
	public void setCost(int cost) {
		this.cost = cost;// sets the cost of the connection to that provided
	}// setCost(int) method
	
	// getFromNode method - allows the originating node of the connection to be accessed
	public int getFromNode() {
		return this.fromNode;// returns the originating node
	}// getFromNode() method
	
	// getToNode method - allows the ending node of the connection to be accessed
	public int getToNode() {
		return this.toNode;// returns the ending node of the connection
	}// getToNode() method
	
	// toString method - overwrites the toString method in the Object class, formats the org.hzt.ant.Connection for printing
	public String toString() {
		return toNode + "";// returns the formatted version of the connection
	}// toString() method
}// org.hzt.ant.Connection class

