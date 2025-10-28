package aradnezami.cambridgesignallingmap.UI.DiagramElements;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.Point;
import java.awt.geom.Path2D;

/**
 * Implementors of the Signal class are used to conveniently draw signal representations on a diagram, using
 * a provided {@link Graphics2D} object, relating to the context being drawn on. Some properties of the signal
 * may be altered after construction, however only to the extent that would be required by a live diagram
 * (eg: change of aspects). All properties may be altered by a diagram editor however. <br>
 *
 * <h3>Orientation</h3>
 * The orientation of a signal determines which way it faces: Left or Right. The orientation is specified by
 * {@link #LEFT} and {@link #RIGHT}
 * <h3>Offset</h3>
 * The offset is used to move a signal slightly up or down on the diagram to avoid touching other elements,
 * namely tracks. These are specified by {@link #NO_OFFSET}, {@link #OFFSET_UP}, {@link #OFFSET_DOWN}
 */
public abstract class Signal {
    /**
     * Used to indicate the event type for a signal
     */
    public static final int ASPECT_TYPE = 0;
    public static final int ROUTED_TYPE = 7;

    // Aspect states
    public static final int ON = 0;
    public static final int MAIN_OFF = 1;
    public static final int SHUNT_OFF = 2;
    public static final int BOTH_OFF = 3;
    public static final int UNKNOWN = 4;

    // Route states
    public static final int ROUTE_SET = 0;
    public static final int ROUTE_NOT_SET = 1;


    public static final int LEFT = 0;
    public static final int RIGHT = 1;

    public static final int NO_OFFSET = 0;
    public static final int OFFSET_UP = -1;
    public static final int OFFSET_DOWN = 1;


    protected static final Color DEFAULT_POST_COLOUR = new Color(100, 100, 100);
    protected static final Color ROUTED_POST_COLOUR = new Color(220, 220, 220);

    protected static final Color MAIN_ON_COLOUR = new Color(170, 0, 0);
    protected static final Color MAIN_OFF_COLOUR = new Color(0, 140, 0);
    protected static final Color SUBSHUNT_ON_COLOUR = new Color(100, 100, 100);
    protected static final Color SOLOSHUNT_ON_COLOUR = new Color(170, 0, 0);
    protected static final Color SHUNT_OFF_COLOUR = new Color(220, 220, 220);
    protected static final Color ASPECT_UNKNOWN_COLOUR = new Color(100, 100, 100);


    @NotNull
    public final String name;

    public final int x;
    public final int y;
    @MagicConstant(intValues = {LEFT, RIGHT})
    public final int orientation;
    @MagicConstant(intValues = {NO_OFFSET, OFFSET_UP, OFFSET_DOWN})
    public final int offset;


    protected int aspectState;
    @MagicConstant(intValues = {ROUTE_SET, ROUTE_NOT_SET})
    protected int routedState;


    /**
     * Sets the aspect state of the signal
     * @param state State of the signal
     * @throws IllegalArgumentException If the state is not valid for the signal implementation
     */
    abstract public void setAspectState(int state);

    /**
     * Sets the routed state of the signal
     * @param state Routed state of signal
     * @throws IllegalArgumentException If the state is not {@link #ROUTE_SET} or {@link #ROUTE_NOT_SET}
     */
    public void setRoutedState(@MagicConstant(intValues = {ROUTE_SET, ROUTE_NOT_SET}) int state) {
        if (state != ROUTE_SET && state != ROUTE_NOT_SET) {
            throw new IllegalArgumentException(state + " is not a valid routed state. Route="+name);
        }

        this.routedState = state;
    }




    /**
     * Draws the signal on the provided graphics context
     * @param g2d Context to draw on
     */
    abstract public void draw(Graphics2D g2d);


    /**
     * Creates a signal with the following properties
     * @throws IllegalArgumentException If offset is not {@link #NO_OFFSET}, {@link #OFFSET_UP} or {@link #OFFSET_DOWN}
     * or if orientation is not {@link #LEFT} or {@link #RIGHT}
     */
    public Signal(@NotNull String name,
                  int x,
                  int y,
                  @MagicConstant(intValues = {NO_OFFSET, OFFSET_UP, OFFSET_DOWN}) int offset,
                  @MagicConstant(intValues = {LEFT, RIGHT}) int orientation) {

        this.name = name;
        this.x = x;
        this.y = y;

        if (orientation != LEFT && orientation != RIGHT) {
            throw new IllegalArgumentException(orientation + " is not a valid orientation. Route=" + name);
        }
        this.orientation = orientation;

        if (offset !=  NO_OFFSET && offset != OFFSET_UP && offset != OFFSET_DOWN) {
            throw new IllegalArgumentException(offset + " is not a valid offset. Route=" + name);
        }
        this.offset = offset;

        aspectState = UNKNOWN;
        routedState = ROUTE_NOT_SET;
    }



    protected static Point[] signalPost(int x, int y) {
        return new Point[]{
                new Point(x, y+4),
                new Point(x+5, y+4),
                new Point(x+5, y+6),
                new Point(x, y+6)
        };
    }

    protected static Point[] signalMainHead(int x, int y) {
        return new Point[]{
                new Point(x+2, y+2),
                new Point(x+4, y+2),
                new Point(x+6, y+4),
                new Point(x+6, y+6),
                new Point(x+4, y+8),
                new Point(x+2, y+8),
                new Point(x, y+6),
                new Point(x, y+4),
        };
    }

    protected static Point[] signalShuntHead(int x, int y, int orientation) {
        Point[] points;
        if (orientation == LEFT) {
            points = new Point[]{
                    new Point(x, y+2),
                    new Point(x+6, y+2),
                    new Point(x+6, y+8),
                    new Point(x+5, y+8),
                    new Point(x+2, y+6),
                    new Point(x, y+3)
            };
        } else {
            points = new Point[]{
                    new Point(x,y+2),
                    new Point(x+1, y+2),
                    new Point(x+4, y+4),
                    new Point(x+6, y+7),
                    new Point(x+6, y+8),
                    new Point(x, y+8)
            };
        }

        return points;
    }


    protected static void drawShape(Point[] points, Graphics2D g2d, Color colour) {
        g2d.setColor(colour);
        g2d.fill(createShape(points));
    }

    protected static void drawLine(Point[] points, Graphics2D g2d, Color colour) {
        g2d.setColor(colour);
        g2d.draw(createShape(points));
    }

    private static Path2D createShape(Point[] points) {
        Path2D path = new Path2D.Double();
        path.moveTo(points[0].x, points[0].y);
        for (Point point : points) {
            path.lineTo(point.x, point.y);
        }
        path.closePath();

        return path;
    }


    protected static Point[] scale(Point[] points, @MagicConstant double scale) {
        for (Point point : points) {
            point.x = (int) Math.ceil(point.x * scale);
            point.y = (int) Math.ceil(point.y * scale);
        }

        return points;
    }


    protected static Point[] applyOffset(Point[] points, int offsetType) {
        for (Point point: points) {
            point.y += offsetType;
        }
        return points;
    }

    /**
     * Returns a string representation of the signal state <br>
     * Eg: {@code Signal.translateState(}{@link #MAIN_OFF Signal.MAIN_OFF})
     * returns "Main off"
     * @param state constant representing the signal state
     * @return string representation of equipment state
     */
    public static String translateAspectState(int state) {
        return switch (state) {
            case ON -> "On";
            case MAIN_OFF -> "Main off";
            case SHUNT_OFF -> "Shunt off";
            case BOTH_OFF -> "Both off";
            default -> throw new IllegalArgumentException(state + " is not a valid Signal state");
        };
    }

    public static String translateRoutedState(int state) {
        return switch (state) {
            case ROUTE_SET -> "Set";
            case ROUTE_NOT_SET -> "Not Set";
            default -> throw new IllegalArgumentException(state + " is not a valid Route indicator state");
        };
    }


    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public int getOffset() {
        return offset;
    }

    public int getOrientation() {
        return orientation;
    }
}
