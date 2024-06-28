package aradnezami.cambridgesignallingmap.Diagram;

public class TrackCircuit {
    /**
     * Used to indicate the event type
     */
    public static final int TRACKCIRCUIT_TYPE = 2;
    
    public static final int UNOCCUPIED = 0;
    public static final int OCCUPIED = 1;
    
    /**
     * Returns a string representation of the track circuit state <br>
     * Eg: {@code TrackCircuit.translateState(}{@link TrackCircuit#OCCUPIED TrackCircuit.OCCUPIED})
     * returns "Occupied"
     * @param state constant representing the track circuit state
     * @return string representation of equipment state
     */
    public static String translateState(int state) {
        return switch (state) {
            case UNOCCUPIED -> "Unnoccupied";
            case OCCUPIED -> "Occupied";
            default -> throw new IllegalArgumentException(state + " is not a valid Track circuit state");
        };
    }
}
