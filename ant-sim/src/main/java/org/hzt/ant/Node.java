package org.hzt.ant;/*
 * org.hzt.ant.Node class - used in org.hzt.ant.Board class, represents tile positions in the org.hzt.ant.Game
 */

import java.awt.Image;// imports library necessary to provide an image for the org.hzt.ant.Game tile
import java.awt.Point;// imports the library necessary to use a Point to represent the org.hzt.ant.Game tile position
import java.util.List;// imports library necessary to use List for the Connections associated with each org.hzt.ant.Node

public class Node {
	private Point tile;// represents the position of the org.hzt.ant.Game tile in the org.hzt.ant.Game space
	private Image image;// represents the image of the entity occupying the org.hzt.ant.Game tile
	private boolean occupied;// represents the cost associated with moving across the org.hzt.ant.Game tile
	private List<Connection> connections;// represents the Connections to other org.hzt.ant.Game tiles
	private String occupant;// represents the object that is currently at the node
	
	// org.hzt.ant.Node constructor - no parameters, creates an empty org.hzt.ant.Node
	public Node() {
		this.occupied = false;// assigns a cost of -1, used as a marker value
	}// org.hzt.ant.Node() constructor
	
	// org.hzt.ant.Node constructor - position of the tile, image of entity, cost to cross, Connections to other tiles
	public Node (final Point tile, final Image image, final boolean occupied, final String occupant) {
		this.tile = tile;// sets the position to that provided
		this.image = image;// sets the image to that of the entity occupying the tile
		this.occupied = occupied;// sets the cost to cross the tile
		this.connections = null;// sets to null, gets connections later
		this.occupant = occupant;
	}// org.hzt.ant.Node (Point, Image, int, List<org.hzt.ant.Connection>
	
	//getTile method - allows the position of the tile to be accessed
	public Point getTile() {
		return this.tile;// returns the position of the tile
	}// getTile() method
	
	//getImage method - allows the image of the tile to be accessed
	public Image getImage() {
		return this.image;// returns the image of the tile
	}// getImage() method
	
	//setImage method - allows the image of the tile to be set to a specified image
	public void setImage(final Image image) {
		this.image = image;// sets the image of the tile to that specified
	}// setImage(Image) method
	
	//getCost method - allows the cost of the tile to be accessed
	public boolean isOccupied() {
		return this.occupied;// returns the cost of the tile
	}// getCost() method
	
	//getConnections method - allows the connections of the tile to be accessed
	public List<Connection> getConnections() {
		return this.connections;// returns the connections of the tile
	}// getConnections() method
	
	// setOccupant method - allows the occupant variable to be modified
	public void setOccupant(final String occupant) {
		this.occupant = occupant;// sets the occupant to that specified
	}// setOccupant(String) method
	
	// getOccupant method - allows the occupant variable to be accessed
	public String getOccupant() {
		return occupant;// returns the occupant variable
	}// getOccupant() method
}// org.hzt.ant.Node class