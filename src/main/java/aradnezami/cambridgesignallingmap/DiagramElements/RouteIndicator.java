package aradnezami.cambridgesignallingmap.DiagramElements;

public class RouteIndicator {
    /**
     * Used to indicate the event type for a route button indicator
     */
    public static final int TYPE = 3;

    /**
     * A signal who's has had a route set from it. The signaller has pressed this signal as an ENtry signal
     * to set a route from it. The button lights up indicating a route is set from it, which this shows
     */
    public static final int PRESSED = 1;
    
    /**
     * A signal with no route set from it
     * @see #PRESSED
     */
    public static final int RELEASED = 2;
    
    public static String translateState(int state) {
        return switch (state) {
            case PRESSED -> "Pressed";
            case RELEASED -> "Released";
            default -> throw new IllegalArgumentException(state + " is not a valid Route indicator state");
        };
    }
}
