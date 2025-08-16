package org.hzt.ant;

/**
 * @param node               the node number
 * @param connection         connection that connects the node to the path
 * @param costSoFar          cost up to this node in the path
 * @param estimatedTotalCost cost estimated from this node to the end of the path
 */
public record NodeRecord(int node, Connection connection, int costSoFar, int estimatedTotalCost) {
    public NodeRecord() {
        this(0, null, 0, 0);// no estimated cost, blank record
    }
}
