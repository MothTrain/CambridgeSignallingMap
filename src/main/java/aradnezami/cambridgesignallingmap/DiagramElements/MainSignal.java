package aradnezami.cambridgesignallingmap.DiagramElements;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.awt.Point;
import java.awt.*;

/**
 * The MainSignal class is used to conveniently draw signal representations, showing only main aspects on a
 * diagram, using a provided {@link Graphics2D} object, relating to the context being drawn on. Some
 * properties of the signal may be altered after construction, however only to the extent that would be required
 * by a live diagram (eg: change of aspects). All properties may be altered by a diagram editor however. <br>
 *
 * <h3>Orientation</h3>
 * The orientation of a signal determines which way it faces: Left or Right. The orientation is specified by
 * {@link #LEFT} and {@link #RIGHT}
 * <h3>Offset</h3>
 * The offset is used to move a signal slightly up or down on the diagram to avoid touching other elements,
 * namely tracks. These are specified by {@link #NO_OFFSET}, {@link #OFFSET_UP}, {@link #OFFSET_DOWN}
 */
public class MainSignal extends Signal {
    /**
     * Creates a main signal with the following properties
     * @throws IllegalArgumentException If offset is not {@link #NO_OFFSET}, {@link #OFFSET_UP} or {@link #OFFSET_DOWN}
     * or if orientation is not {@link #LEFT} or {@link #RIGHT}
     */
    public MainSignal(@NotNull String name,
                      int x,
                      int y,
                      @MagicConstant(intValues = {NO_OFFSET, OFFSET_UP, OFFSET_DOWN}) int offset,
                      @MagicConstant(intValues = {LEFT, RIGHT}) int orientation) {

        super(name, x, y, offset ,orientation);
    }


    /**
     * Sets the aspect state of the signal
     * @param state State of the signal
     * @throws IllegalArgumentException If the state is not {@link #MAIN_OFF}, {@link #ON} or {@link #UNKNOWN}
     */
    @Override
    public void setAspectState(int state) {
        if (state != MAIN_OFF && state != ON && state != UNKNOWN) {
            throw new IllegalArgumentException(state + " is not a valid main signal state. Signal="+name);
        }

        aspectState = state;
    }

    /**
     * Draws the signal on the provided graphics context
     * @param g2d Context to draw on
     */
    @Override
    public void draw(Graphics2D g2d) {
        int signalPostX = (orientation == LEFT) ? x+5 : x;
        int signalHeadX = (orientation == LEFT) ? x-1 : x+5;
        Point[] signalPost = signalPost(signalPostX, y);
        Point[] signalHead = signalMainHead(signalHeadX, y);

        applyOffset(signalPost, offset);
        applyOffset(signalHead, offset);

        scale(signalHead, ElementCollection.scale);
        scale(signalPost, ElementCollection.scale);


        switch (aspectState) {
            case ON: drawShape(signalHead, g2d, MAIN_ON_COLOUR); break;
            case MAIN_OFF: drawShape(signalHead, g2d, MAIN_OFF_COLOUR); break;
            case UNKNOWN: drawLine(signalHead, g2d, ASPECT_UNKNOWN_COLOUR); break;
            default:
        }

        Color postColour = (routedState == ROUTE_SET) ? Signal.ROUTED_POST_COLOUR : Signal.DEFAULT_POST_COLOUR;
        drawShape(signalPost, g2d, postColour);
    }
}
