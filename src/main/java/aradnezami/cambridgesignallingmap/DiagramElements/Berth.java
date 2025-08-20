package aradnezami.cambridgesignallingmap.DiagramElements;


import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * A berth is used to conveniently draw train headcodes, contained within a berth. This class
 * draws both the train describer {@link Text} and a backing rectangle to make the train describer
 * more visible.
 */
public class Berth {
    private final static int font = Text.HEADCODE_FONT;
    private final static int fontSize = 8;
    private final static int offsetText = -5;
    private static final int BACKER_WIDTH = 23;

    private final static Color headcodeColour = new Color(0, 150, 150);


    @NotNull
    public final String name;

    private final Text describer;

    private final int x;
    private final int y;


    /**
     * Creates an instance of the berth class with the given properties
     * @param name The name of the berth. Should be 4 characters
     * @param x The x coordinate of the the left of the describer
     * @param y The y coordinate of the <b>middle</b> of the describer
     * @throws IllegalArgumentException If the name was not 4 characters
     * @throws FontLoadingException If the text fonts could not be loaded
     */
    public Berth(@NotNull String name,
                 int x,
                 int y) throws FontLoadingException {

        if (name.length() != 4) {
            throw new IllegalArgumentException("Berth name must contain exactly 4 characters. Name="+name);
        }

        this.name = name;
        this.x = x;
        this.y = y;

        describer = new Text(
                "",
                x,
                y+offsetText,
                headcodeColour,
                fontSize,
                font
        );
    }


    /**
     * Draws the berth and its describer on the diagram. If the berth is empty (i.e: the describer
     * is an empty string), nothing (including the backing rectangle) will be drawn
     *
     * @param g2d The graphics context to draw on
      */
    public void draw(Graphics2D g2d) {
        if (describer.text.isEmpty()) {return;}

        g2d.setColor(Color.black);
        g2d.fillRect((int) ((x-1)*ElementCollection.scale),
                (int) ((y-((double) fontSize/2))*ElementCollection.scale),
                (int) (BACKER_WIDTH *ElementCollection.scale),
                (int) (fontSize*ElementCollection.scale));

        describer.draw(g2d);

    }

    /**
     * Sets the train describer contained within the berth
     * @param describer The new train describer or an empty string if the describer is empty
     * @throws IllegalArgumentException If the describer does not have a length of 4 or 0
     */
    public void setDescriber(@NotNull String describer) {
        if (describer.length() != 4 && describer.length() != 0) {
            throw new IllegalArgumentException("Berth name must contain exactly 4 or 0 characters. Name="+name);
        }

        this.describer.text = describer;
    }
}
