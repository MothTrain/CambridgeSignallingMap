package aradnezami.cambridgesignallingmap.DiagramElements;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.Point;

/**
 * The MainSignal class is used to conveniently draw signals representations, showing both shunt and main aspects
 * on a diagram, using a provided {@link Graphics2D} object, relating to the context being drawn on. Some
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
public class CompoundSignal extends Signal {
    /**
     * Sets the aspect state of the signal
     *
     * @param state State of the signal
     * @throws IllegalArgumentException If the state is not {@link #MAIN_OFF}, {@link #SHUNT_OFF}, {@link #BOTH_OFF},
     * {@link #ON} or {@link #UNKNOWN}
     */
    @Override
    public void setAspectState(int state) {
        if (state != MAIN_OFF &&
                state != ON &&
                state != UNKNOWN &&
                state != SHUNT_OFF &&
                state != BOTH_OFF) {
            throw new IllegalArgumentException(state + " is not a valid compound signal state. Signal="+name);
        }

        aspectState = state;
    }

    /**
     * Draws the signal on the provided graphics context
     *
     * @param g2d Context to draw on
     */
    @Override
    public void draw(Graphics2D g2d) {
        int signalPostX = (orientation == LEFT) ? x+12 : x-1;
        int shuntSignalHeadX = (orientation == LEFT) ? x+6 : x+4;
        int mainSignalHeadX = (orientation == LEFT) ? x : x+10;

        Point[] signalPost = signalPost(signalPostX, y);
        Point[] shuntSignalHead = signalShuntHead(shuntSignalHeadX, y, orientation);
        Point[] mainSignalHead = signalMainHead(mainSignalHeadX, y);

        applyOffset(signalPost, offset);
        applyOffset(shuntSignalHead, offset);
        applyOffset(mainSignalHead, offset);

        scale(shuntSignalHead, 1.5);
        scale(signalPost, 1.5);
        scale(mainSignalHead, 1.5);

        switch (aspectState) {
            case ON:
                drawShape(mainSignalHead, g2d, MAIN_ON_COLOUR);
                drawShape(shuntSignalHead, g2d, SUBSHUNT_ON_COLOUR);
                break;
            case SHUNT_OFF:
                drawShape(mainSignalHead, g2d, MAIN_ON_COLOUR);
                drawShape(shuntSignalHead, g2d, SHUNT_OFF_COLOUR);
                break;
            case MAIN_OFF:
                drawShape(mainSignalHead, g2d, MAIN_OFF_COLOUR);
                drawShape(shuntSignalHead, g2d, SUBSHUNT_ON_COLOUR);
                break;
            case BOTH_OFF:
                drawShape(mainSignalHead, g2d, MAIN_OFF_COLOUR);
                drawShape(shuntSignalHead, g2d, SHUNT_OFF_COLOUR);
                break;
            case UNKNOWN:
                drawLine(mainSignalHead, g2d, ASPECT_UNKNOWN_COLOUR);
                drawLine(shuntSignalHead, g2d, ASPECT_UNKNOWN_COLOUR);
                break;
            default:
        }

        Color postColour = (routedState == ROUTE_SET) ? Signal.ROUTED_POST_COLOUR : Signal.DEFAULT_POST_COLOUR;
        drawShape(signalPost, g2d, postColour);
    }

    /**
     * Creates a signal with the following properties
     *
     * @throws IllegalArgumentException If offset is not {@link #NO_OFFSET}, {@link #OFFSET_UP} or {@link #OFFSET_DOWN}
     *                                  or if orientation is not {@link #LEFT} or {@link #RIGHT}
     */
    public CompoundSignal(@NotNull String name, int x, int y, int offset, int orientation) {
        super(name, x, y, offset, orientation);
    }
}
