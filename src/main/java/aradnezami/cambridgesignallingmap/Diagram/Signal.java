package aradnezami.cambridgesignallingmap.Diagram;

public class Signal {
    /**
     * Used to indicate the event type for a signal
     */
    public static final int TYPE = 0;

    /**
     * Represents a signal showing a clear (green) aspect. Used when DGK = 1
     */
    public static final int CLEAR = 0;
    /**
     * Represents a signal that is off, this means it is showing anything except red. It could
     * be green, yellow or double yellow but do not know. Used when RGK = 0 or OFFK = 1 and no
     * DGK is mapped
     */
    public static final int OFF = 1;
    /**
     * Represents a signal showing a cautionary aspect. This could be yellow or double
     * yellow. Used when DGK=0 and RGK=0/OFFK=1
     */
    public static final int CAUTION = 2;
    /**
     * Represents a signal showing anything except clear (green), could be danger, yellow,
     * double yellow. Used when DGK=0 and no RGK or OFFK is mapped
     */
    public static final int RESTRICTIVE = 3;
    /**
     * Represents a signal showing a danger (red) aspect. Used when RGK=1
     */
    public static final int DANGER = 4;
    /**
     * Used when data provided shows a signal aspect that is not possible for example:
     * DGK=1 and RGK=1/OFFK=0 (Signal is green and red: obviously not possible)
     */
    public static final int ERRONEOUS = 5;
    
    /**
     * Returns a string representation of the signal state <br>
     * Eg: {@code Signal.translateState(}{@link Signal#OFF Signal.OFF})
     * returns "Off"
     * @param state constant representing the signal state
     * @return string representation of equipment state
     */
    public static String translateState(int state) {
        return switch (state) {
            case CLEAR -> "Clear";
            case OFF -> "Off";
            case CAUTION -> "Caution";
            case RESTRICTIVE -> "Restrictive";
            case DANGER -> "Danger";
            case ERRONEOUS -> "Erroneous";
            default -> throw new IllegalArgumentException(state + " is not a valid Signal state");
        };
    }
}
