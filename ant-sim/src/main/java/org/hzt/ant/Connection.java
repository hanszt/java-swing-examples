package org.hzt.ant;

/**
 * @param fromNode represents the node that the connection originates at
 * @param toNode   represents the node that the connection ends at
 * @param cost     represents the cost of using the connection
 */
public record Connection(int fromNode, int toNode, int cost) {
}

