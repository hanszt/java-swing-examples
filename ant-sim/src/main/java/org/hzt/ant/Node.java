package org.hzt.ant;

import java.awt.*;

/**
 * Represents tile positions in the Game
 */
public final class Node {
	private Point tile;
	private Image image;
	private final boolean occupied;
	private String occupant;
	
	public Node() {
		this.occupied = false;
	}
	
    public Node (final Point tile, final Image image, final boolean occupied, final String occupant) {
		this.tile = tile;
		this.image = image;
		this.occupied = occupied;
		this.occupant = occupant;
	}
	
	public Point getTile() {
		return this.tile;
	}
	
	public Image getImage() {
		return this.image;
	}
	
	public void setImage(final Image image) {
		this.image = image;
	}
	
	public boolean isOccupied() {
		return this.occupied;
	}

	public void setOccupant(final String occupant) {
		this.occupant = occupant;
	}

	public String getOccupant() {
        return occupant;
    }
}