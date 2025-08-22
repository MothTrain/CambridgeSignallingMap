package aradnezami.cambridgesignallingmap.DiagramElements;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * The rectangle class is used to draw rectangles (primarily platforms) on a given graphics context.
 * A rectangle has an 'A' and 'B' Coordinate. The 'A' coordinate must always be the top left corner
 * and the 'B' coordinate must always be the bottom right coordinate
 */
public class Rectangle {
    public static final Color PLATFORM_COLOR = new Color(255, 127, 0);

    @NotNull
    public final String name;

    @NotNull
    private final Color colour;

    public final int A_x;
    public final int A_y;
    public final int B_x;
    public final int B_y;


    /**
     * Creates a rectangle with the following properties
     * @param name Name of the rectangle
     * @param a_x x coordinate of the left side
     * @param a_y y coordinate of the right edge
     * @param b_x x coordinate of the right edge
     * @param b_y y coordinate of the bottom edge
     * @param colour Colour of the rectangle
     *
     * @throws IllegalStateException If a_x > b_x or if a_y > b_y
     */
    public Rectangle(@NotNull String name,
                     int a_x,
                     int a_y,
                     int b_x,
                     int b_y,
                     @NotNull Color colour) {

        this.name = name;

        this.A_x = a_x;
        this.A_y = a_y;
        this.B_x = b_x;
        this.B_y = b_y;

        this.colour = colour;
    }

    /**
     * Draws the rectangle on the given graphics context
     * @param g2d The graphics context to draw on
     */
    public void draw(Graphics2D g2d) {
        g2d.setColor(colour);
        g2d.fillRect((int) Math.ceil(A_x*ElementCollection.scale),
                (int) Math.ceil(A_y*ElementCollection.scale),
                (int) ((B_x - A_x)*ElementCollection.scale),
                (int) ((B_y - A_y)*ElementCollection.scale));
    }
}
