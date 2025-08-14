package aradnezami.cambridgesignallingmap.DiagramElements;


import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;

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
    private String name;
    @NotNull
    public DatumPoint datumPoint;

    private final Text describer;

    public int x;
    public int y;


    /**
     * Creates an instance of the berth class with the given properties
     * @throws IOException If there was an IO error retrieving a font file
     * @throws FontFormatException If the font file was incorrectly formatted
     */
    public Berth(@NotNull String name,
                 @NotNull DatumPoint datumPoint,
                 int x,
                 int y) throws IOException, FontFormatException {

        if (name.length() != 4) {
            throw new IllegalArgumentException("Berth name must contain exactly 4 characters. Name="+name);
        }

        this.name = name;
        this.datumPoint = datumPoint;
        this.x = x;
        this.y = y;

        describer = new Text(
                "",
                datumPoint,
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
        g2d.fillRect((int) ((x+datumPoint.x-1)*1.5),
                (int) ((y+datumPoint.y-((double) fontSize/2))*1.5),
                (int) (BACKER_WIDTH *1.5),
                (int) (fontSize*1.5));

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
