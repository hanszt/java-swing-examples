package org.hzt.ant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The AStar class implements the A* algorithm for finding the shortest path
 * in a graph between a start node and an end node. The A* algorithm is a well-known
 * graph traversal and search algorithm that uses heuristics to optimize the search
 * for the shortest path.
 * <p>
 * This class provides static utility methods for navigating the graph, estimating
 * costs, and constructing the shortest path. The A* algorithm combines a cost-so-far
 * approach with a heuristic that estimates the remaining cost to reach the goal.
 * <p>
 * Key functionalities include:
 * - Finding the shortest path to the target using a heuristic function.
 * - Managing open and closed lists of nodes to optimize graph traversal.
 * - Reconstructing the shortest path once the goal is reached.
 */
public final class AStar {
    private static final List<NodeRecord> openList = new ArrayList<>();

    public static List<Connection> pathFindAStar(
            final Graph graph,
            final int start,
            final int end,
            final Heuristic heuristic
    ) {
        final var startRecord = new NodeRecord(start, null, 0, heuristic.estimate(start));

        openList.clear();
        openList.add(startRecord);
        final var closedList = new ArrayList<NodeRecord>();

        var current = new NodeRecord();
        List<Connection> connections;
        int endNode;
        int endNodeCost;
        NodeRecord endNodeRecord;
        int endNodeHeuristic;
        while (!openList.isEmpty()) {
            current = getSmallestElement();

            if (current.node() == end) {
                break;
            } else {
                connections = graph.getConnections(current.node());
            }

            for (final var connection : connections) {
                endNode = connection.toNode();
                endNodeCost = current.costSoFar() + connection.cost();

                if (listContains(closedList, endNode)) {
                    endNodeRecord = findNode(closedList, endNode);

                    if (endNodeRecord.costSoFar() <= endNodeCost) {
                        continue;
                    }

                    closedList.remove(endNodeRecord);

                    endNodeHeuristic = endNodeRecord.estimatedTotalCost() - endNodeRecord.costSoFar();
                } else if (listContains(openList, endNode)) {
                    endNodeRecord = findNode(openList, endNode);

                    if (endNodeRecord.costSoFar() <= endNodeCost) {
                        continue;
                    }

                    endNodeHeuristic = endNodeRecord.estimatedTotalCost() - endNodeRecord.costSoFar();
                } else {
                    endNodeHeuristic = heuristic.estimate(endNode);
                }

                endNodeRecord = new NodeRecord(endNode, connection, endNodeCost, endNodeCost + endNodeHeuristic);

                if (!listContains(openList, endNode)) {
                    openList.add(endNodeRecord);
                }
            }

            openList.remove(current);
            closedList.add(current);
        }
        if (current.node() != end) {
            return Collections.emptyList();
        } else {
            final List<Connection> path = new ArrayList<>();

            while (current.node() != start) {
                path.add(current.connection());
                final var currentNode = current.connection().fromNode();
                for (final var nodeRecord : closedList) {
                    if (nodeRecord.node() == currentNode) {
                        current = nodeRecord;
                    }
                }
            }
            return reverse(path);
        }
    }

    private static NodeRecord getSmallestElement() {
        var smallestElement = openList.getFirst();
        for (final var nodeRecord : openList) {
            if (nodeRecord.estimatedTotalCost() < smallestElement.estimatedTotalCost()) {
                smallestElement = nodeRecord;
            }
        }
        return smallestElement;
    }

    private static boolean listContains(final List<NodeRecord> list, final int node) {
        for (final var listNode : list) {
            if (listNode.node() == node) {
                return true;
            }
        }
        return false;
    }

    private static NodeRecord findNode(final List<NodeRecord> list, final int node) {
        for (final var listNode : list) {
            if (listNode.node() == node) {
                return listNode;
            }
        }
        return null;
    }

    private static List<Connection> reverse(final List<Connection> path) {
        final List<Connection> reverse = new ArrayList<>();
        for (var i = path.size() - 1; i >= 0; i--) {
            reverse.add(path.get(i));
        }
        return reverse;
    }
}
