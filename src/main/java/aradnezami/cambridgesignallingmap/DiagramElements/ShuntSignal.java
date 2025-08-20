package aradnezami.cambridgesignallingmap.DiagramElements;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.awt.Point;
import java.awt.*;

/**
 * The ShuntSignal class is used to conveniently draw signals representations, showing only showing shunt
 * aspects on a diagram, using a provided {@link Graphics2D} object, relating to the context being drawn on. Some
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
public class ShuntSignal extends Signal {
    /**
     * Creates a shunt signal with the following properties
     * @throws IllegalArgumentException If offset is not {@link #NO_OFFSET}, {@link #OFFSET_UP} or {@link #OFFSET_DOWN}
     * or if orientation is not {@link #LEFT} or {@link #RIGHT}
     */
    public ShuntSignal(@NotNull String name,
                      int x,
                      int y,
                      @MagicConstant(intValues = {NO_OFFSET, OFFSET_UP, OFFSET_DOWN}) int offset,
                      @MagicConstant(intValues = {LEFT, RIGHT}) int orientation) {

        super(name, x, y, offset ,orientation);
    }

    /**
     * Sets the aspect state of the signal
     *
     * @param state State of the signal
     * @throws IllegalArgumentException If the state is not {@link #SHUNT_OFF}, {@link #ON} or {@link #UNKNOWN}
     */
    @Override
    public void setAspectState(int state) {
        if (state != SHUNT_OFF && state != ON && state != UNKNOWN) {
            throw new IllegalArgumentException(state + " is not a valid shunt signal state. Signal="+name);
        }

        aspectState = state;
    }

    /**
     * Draws the signal on the provided graphics context
     * @param g2d Context to draw on
     */
    @Override
    public void draw(Graphics2D g2d) {
        int signalPostX = (orientation == LEFT) ? x+6 : x-1;
        int signalHeadX = (orientation == LEFT) ? x : x+4;

        java.awt.Point[] signalPost = signalPost(signalPostX, y);
        Point[] signalHead = signalShuntHead(signalHeadX, y, orientation);

        applyOffset(signalPost, offset);
        applyOffset(signalHead, offset);

        scale(signalHead, ElementCollection.scale);
        scale(signalPost, ElementCollection.scale);

        switch (aspectState) {
            case ON: drawShape(signalHead, g2d, SOLOSHUNT_ON_COLOUR); break;
            case SHUNT_OFF: drawShape(signalHead, g2d, SHUNT_OFF_COLOUR); break;
            case UNKNOWN: drawLine(signalHead, g2d, ASPECT_UNKNOWN_COLOUR); break;
            default:
        }

        Color postColour = (routedState == ROUTE_SET) ? Signal.ROUTED_POST_COLOUR : Signal.DEFAULT_POST_COLOUR;
        drawShape(signalPost, g2d, postColour);
    }
}
