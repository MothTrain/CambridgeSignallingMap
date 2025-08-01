package aradnezami.cambridgesignallingmap.Diagram;

public class Signal {
    /**
     * Used to indicate the event type for a signal
     */
    public static final int TYPE = 0;

    public static final int ON = 0;
    public static final int MAIN_OFF = 1;
    public static final int SHUNT_OFF = 2;
    public static final int BOTH_OFF = 3;
    
    /**
     * Returns a string representation of the signal state <br>
     * Eg: {@code Signal.translateState(}{@link Signal#MAIN_OFF Signal.MAIN_OFF})
     * returns "Main off"
     * @param state constant representing the signal state
     * @return string representation of equipment state
     */
    public static String translateState(int state) {
        return switch (state) {
            case ON -> "On";
            case MAIN_OFF -> "Main off";
            case SHUNT_OFF -> "Shunt off";
            case BOTH_OFF -> "Both off";
            default -> throw new IllegalArgumentException(state + " is not a valid Signal state");
        };
    }
}
