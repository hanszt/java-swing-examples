package org.hzt.ant;/*
 * org.hzt.ant.Graph class - represents a org.hzt.ant.Graph, used to represent the nodes/tiles of the org.hzt.ant.Game board
 */

import java.util.ArrayList;
import java.util.List;// imports libraries necessary to use List for connections

public final class Graph {
	private final List<Connection> connections;// represents the connections/edges of the graph
	
	// org.hzt.ant.Graph constructor - no parameters, creates an empty graph
	public Graph() {
		this.connections = new ArrayList<>();// creates an empty set of connections
	}// org.hzt.ant.Graph() constructor
	
	//getConnections method - no parameters, allows the org.hzt.ant.Graph connections to be accessed
	public List<Connection> getConnections() {
		return this.connections;// returns the connections in the graph
	}// getConnections() method
	
	//getConnections method - fromNode, allows the org.hzt.ant.Graph connections of a specified node to be accessed
	public List<Connection> getConnections(int fromNode) {
		List<Connection> connectionsFromNode = new ArrayList<>();// used to hold connections from the node
		for (Connection connection : this.connections) {// loops through the graph connections
			if (connection.fromNode() == fromNode)// checks to see if they originate at the specified node
				connectionsFromNode.add(connection);// adds connections that originate at the specified node
		}// for (connection)
		return connectionsFromNode;// returns the connections that originate from the specified node
	}// getConnections(int) method
	
	// addConnection method - connection, allows connections to be added to the graph
	public void addConnection(Connection connection) {
		this.connections.add(connection);// adds the connection provided to the graph
	}// addConnection(org.hzt.ant.Connection) method
	
	// addConnections method - allows a set of connections to be added to the graph
	public void addConnections(List<Connection> connection) {
		this.connections.addAll(connection);// adds the provided connections to the graph
	}// addConnections(List<org.hzt.ant.Connection> method
}// org.hzt.ant.Graph class
