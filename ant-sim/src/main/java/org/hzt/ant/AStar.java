package org.hzt.ant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;// imports library necessary for using the List to hold org.hzt.ant.NodeRecord lists

/**
 * The AStar class implements the A* algorithm for finding the shortest path
 * in a graph between a start node and an end node. The A* algorithm is a well-known
 * graph traversal and search algorithm that uses heuristics to optimize the search
 * for the shortest path.
 *
 * This class provides static utility methods for navigating the graph, estimating
 * costs, and constructing the shortest path. The A* algorithm combines a cost-so-far
 * approach with a heuristic that estimates the remaining cost to reach the goal.
 *
 * Key functionalities include:
 * - Finding the shortest path to the target using a heuristic function.
 * - Managing open and closed lists of nodes to optimize graph traversal.
 * - Reconstructing the shortest path once the goal is reached.
 */
public final class AStar {
	private static List<NodeRecord> openList;// represents the list of Nodes that have not been searched
    // represents the list of Nodes that has been searched
	
	// pathFindAStar method - graph, start, end, and heuristic; finds the minimum path
	public static List<Connection> pathFindAStar(final Graph graph, final int start, final int end, final Heuristic heuristic) {
        final var startRecord = new NodeRecord(start, null, 0, heuristic.estimate(start));// creates org.hzt.ant.NodeRecord for start org.hzt.ant.Node
		
		openList = new ArrayList<>();// creates the list for nodes not yet searched
		openList.add(startRecord);// adds the start node record to the open list
		final var closedList = new ArrayList<NodeRecord>();// creates the list for nodes already searched

        var current = new NodeRecord();// creates a node record for the next node
		List<Connection> connections;// will be used to hold the connection associated with the current node
		int endNode;// will be used to hold the end node of the connection
		int endNodeCost;// will be used to hold the cost of the end node of the connection
		NodeRecord endNodeRecord;// will be used to hold the node record of the end node of the connection
		int endNodeHeuristic;// will be used to hold the estimated cost from the end node to the goal node
		while (!openList.isEmpty()) {// cycles through the open list of nodes
			current = getSmallestElement();// gets the node with the smallest estimated total cost
			
			if (current.node() == end) {// breaks if the current node is the goal node
				break;
			} else {// if the current node is not the goal node, gets the connections for the node
				connections = graph.getConnections(current.node());
			}
			
			for (final var connection : connections) {// loops through the connections for the current node
				endNode = connection.toNode();// gets the endNode for the current connection
				endNodeCost = current.costSoFar() + connection.cost();// gets the cost for the endNode of the current connection
				
				if (listContains(closedList, endNode)) {//  checks to see if the endNode is in the closedList
					endNodeRecord = findNode(closedList, endNode);// gets the org.hzt.ant.NodeRecord from the closedList
					
					if (endNodeRecord.costSoFar() <= endNodeCost)// checks to see if the cost so far in the record is smaller than currently calculated
						continue;// continues if there is no point in continuing to check current node, already has a smaller cost path
					
					closedList.remove(endNodeRecord);// removes the endNode record from the closedList
					
					endNodeHeuristic = endNodeRecord.estimatedTotalCost() - endNodeRecord.costSoFar();// gets the estimated cost for the current end node
				} else if (listContains(openList, endNode)) {// checks if the end node is in the openList
					endNodeRecord = findNode(openList, endNode);// retrieves the record for the end node from the openList
					
					if (endNodeRecord.costSoFar() <= endNodeCost)// checks if the cost in the record is smaller than that being calculated
						continue;// continues if the record has a smaller cost than currently being calculated
					
					endNodeHeuristic = endNodeRecord.estimatedTotalCost() - endNodeRecord.costSoFar();// gets the cost for the endNode of the current connection
				} else {// if the node record is not in either the open or closed list
					endNodeHeuristic = heuristic.estimate(endNode);// gets the cost estimate for the node
				}
				
				endNodeRecord = new NodeRecord(endNode, connection, endNodeCost, endNodeCost + endNodeHeuristic);// creates record for current node
				
				if (!listContains(openList, endNode))// checks if the openList does not contain the node record
					openList.add(endNodeRecord);// adds the node record to the openList
			}
			
			openList.remove(current);// removes the current node record from the openList
			closedList.add(current);// adds the current node record to the closedList
		}
		
		if (current.node() != end) {// checks if the current node is not the goal node
			return Collections.emptyList();// return null, no path has been found
		} else {// if a path has been found
			final List<Connection> path = new ArrayList<>();// will be used to hold the path
			
			while (current.node() != start) {// while there are more nodes in the path
				path.add(current.connection());// adds the current connection to the path
                final var currentNode = current.connection().fromNode();// gets the from node from the current connection
				for (final var nodeRecord : closedList) {// cycles through the closedList
					if (nodeRecord.node() == currentNode)// checks the node records for the current node
						current = nodeRecord;// sets the node record if found for the current node
				}// for (closedList)
			}// while (!start)
			
			return reverse(path);// class method to get path in correct order
		}// if (end)
	}// pathFindAStar(org.hzt.ant.Graph, int, int, org.hzt.ant.Heuristic) method
	
	// getSmallestElement method - no parameters, gets the node with the smallest estimated cost
	private static NodeRecord getSmallestElement() {
        var smallestElement = openList.get(0);// gets first node record from the openList
		for (final var nodeRecord : openList) {// cycles through the node records in the openList
			if (nodeRecord.estimatedTotalCost() < smallestElement.estimatedTotalCost())// checks if current record has lower estimate
				smallestElement = nodeRecord;// gets record for smallest estimate node
		}// for (openList)
		return smallestElement;// returns the nodeRecord for the node with the smallest estimate
	}// getSmallestElement() method
	
	// listContains method - list and node, checks if the given list contains the given node
	private static boolean listContains(final List<NodeRecord> list, final int node) {
		for (final var listNode : list) {// loops through the list
			if (listNode.node() == node)// checks for the given node
				return true;// returns true if the node is found
		}// for (list)
		return false;// returns false if the node is not found
	}// listContains(List<org.hzt.ant.NodeRecord>, int) method
	
	// findNode method - list and node, finds a node record for a given node from a given list
	private static NodeRecord findNode(final List<NodeRecord> list, final int node) {
		for (final var listNode : list) {// loop through the list
			if (listNode.node() == node)// checks the current list element against the node
				return listNode;// returns the node record when found
		}// for (list)
		return null;// returns null if the node is not found
	}// findNode(List<org.hzt.ant.NodeRecord>, int) method
	
	// reverse method - search path, reverses the path provided 
	private static List<Connection> reverse(final List<Connection> path) {
		final List<Connection> reverse = new ArrayList<>();// will hold the reversed path
		for (var i = path.size() - 1; i >= 0; i--)// loops through the connection in the path
			reverse.add(path.get(i));// adds each connection to the reverse path
		return reverse;// returns the reverse version of the path
	}// reverse(List<org.hzt.ant.Connection>)
}// org.hzt.ant.AStar class
