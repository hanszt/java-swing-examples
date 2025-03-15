package org.hzt;

/**
 * Copyright 2017, Shoumyo Chakravorti, All rights reserved.
 * <p>
 * Licensed under the MIT License.
 * <p>
 * The Cubie class defines the attributes and actions of individual cubies. By defining individual cubies
 * based on their position, colors, and the directions of their colors, a cube can be constructed in the
 * Cube class.
 *
 * @author Shoumyo Chakravorti
 * @version 2.0
 */
public final class Cubie {

    //Store x, y, and z positions of a cubie
    private final int x;
    private final int y;
    private final int z;
    private final boolean corner;
    private final boolean edge;
    //Store the set of colors associated with a cubie; accessible to all subclasses
    private CubieColor[] colors;

    /**
     * Constructs a Cubie object
     * Sets the location of the cubie
     *
     * @param xPos     the x position of the cubie
     * @param yPos     the y position of the cubie
     * @param zPos     the z position of the cubie
     * @param nColors  the colors which the cubie will hold
     * @param isCorner whether the cubie is a corner cubie
     * @param isEdge   whether the cubie is an edge cubie
     */
    public Cubie(
            final int xPos,
            final int yPos,
            final int zPos,
            final CubieColor[] nColors,
            final boolean isCorner,
            final boolean isEdge
    ) {
        x = xPos;
        y = yPos;
        z = zPos;
        corner = isCorner;
        edge = isEdge;
        colors = nColors;
    }

    /**
     * @return x location of cubie
     */
    public int getX() {
        return x;
    }

    /**
     * @return y location of cubie
     */
    public int getY() {
        return y;
    }

    /**
     * @return z location of cubie
     */
    public int getZ() {
        return z;
    }

    /**
     * Finds and returns the direction of a particular color on any type of cubie
     *
     * @param color The color for which the direction is being found
     * @return the direction of the color on the corresponding cubie ('A' if color is not on cubie)
     */
    public char getDirOfColor(final char color) {
        for (final var cubieColor : colors) {
            if (cubieColor.getColor() == color)
                return cubieColor.getDir();
        }
        return 'A';
    }

    /**
     * Finds and returns the color in a particular direction on any type of cubie
     *
     * @param dir The direction for which the color is being found
     * @return the direction of the color on the corresponding cubie ('A' if cubie does not have a color in direction dir)
     */
    public char getColorOfDir(final char dir) {
        for (final var color : colors) {
            if (color.getDir() == dir)
                return color.getColor();
        }
        return 'A';
    }

    /**
     * @return CubieColor[] the colors of the Cubie and their respective directions
     */
    public CubieColor[] getColors() {
        return colors;
    }

    /**
     * Sets the colors of the cubie to those inputed as an array of CubieColors.
     *
     * @param newColors the colors that will be applied to the cubie
     */
    public void setColors(final CubieColor[] newColors) {
        this.colors = newColors;
    }

    /**
     * Changes the color in the given direction.
     *
     * @param dir:    direction
     * @param ncolor: new color
     */
    public void setColorOfDir(final char dir, final char ncolor) {
        for (var i = 0; i < colors.length; i++) {
            if (colors[i].getDir() == dir)
                colors[i].setColor(ncolor);
        }
    }

    /**
     * Returns whether the cubie is a corner cubie
     *
     * @return whether corner cubie
     */
    public boolean isCornerCubie() {
        return corner;
    }

    /**
     * Returns whether the cubie is an edge cubie
     *
     * @return whether edge cubie
     */
    public boolean isEdgeCubie() {
        return edge;
    }

    /**
     * Used to aid formation of the white cross
     *
     * @param x the x position of the cubie
     * @param y the y position of the cubie
     * @return For any EdgeCubie that is NOT in the E Slice, returns the vertical slice that cubie belongs in
     */
    public char verticalFace(final int x, final int y) {
        if (edge) {
            if (x == 0) return 'L';
            else if (x == 1) {
                if (y == 0) {
                    return 'F';
                } else return 'B';
            } else return 'R';
        }
        return 'A';

    }

    /**
     * If the cubie is a corner cubie, method returns whether the cubie is a white corner
     * Returns false if cubie is not a corner cubie
     *
     * @return whether corner cubie
     */
    public boolean isWhiteCorner() {
        if (corner) {
            return (colors[0].getColor() == 'W' || colors[1].getColor() == 'W' || colors[2].getColor() == 'W');
        }
        return false;
    }

}
