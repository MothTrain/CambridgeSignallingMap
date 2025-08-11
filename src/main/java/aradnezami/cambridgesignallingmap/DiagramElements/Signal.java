package aradnezami.cambridgesignallingmap.DiagramElements;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

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

    // Route states
    public static final int ROUTE_SET = 0;
    public static final int ROUTE_NOT_SET = 1;



    public static final int LEFT = 0;
    public static final int RIGHT = 1;

    @NotNull
    public String name;
    @NotNull
    public DatumPoint datumPoint;


    public int x;
    public int y;
    @MagicConstant(intValues = {LEFT, RIGHT})
    private int orientation;


    private int aspectState;
    @MagicConstant(intValues = {ROUTE_SET, ROUTE_NOT_SET})
    private int routedState;


    public Signal(@NotNull String name,
                  @NotNull DatumPoint datumPoint,
                  int x,
                  int y,
                  int orientation) {
        this.name = name;
        this.datumPoint = datumPoint;
        this.x = x;
        this.y = y;

        if (orientation != LEFT && orientation != RIGHT) {
            throw new IllegalArgumentException(orientation + " is not a valid orientation. Route=" + name);
        }
    }



    abstract public void setAspectState(int state);

    public void setRoutedState(@MagicConstant(intValues = {ROUTE_SET, ROUTE_NOT_SET}) int state) {
        if (state != ROUTE_SET && state != ROUTE_NOT_SET) {
            throw new IllegalArgumentException(state + " is not a valid routed state. Route="+name);
        }

        this.routedState = state;
    }


    abstract public void draw(Graphics2D g2d);




    public void setOrientation(@MagicConstant(intValues = {LEFT, RIGHT}) int orientation) {
        if (orientation != LEFT && orientation != RIGHT) {
            throw new IllegalArgumentException(orientation + " is not a valid orientation. Route="+name);
        }

        this.orientation = orientation;
    }

    public int getOrientation() {
        return orientation;
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
}
