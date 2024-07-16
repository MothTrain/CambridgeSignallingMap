package aradnezami.cambridgesignallingmap.Diagram;

public class Point {
    /**
     * Used to indicate the event type for a point
     */
    public static final int TYPE = 1;
    
    /**
     * Represents a point in the normal (straight) position
     */
    public static final int NORMAL = 0;
    /**
     * Represents a point in the reverse position
     */
    public static final int REVERSE = 1;
    /**
     * Used when the normal (NK) and reverse (RK) are both 0. IE: The point is neither normal nor reverse.
     * The use of this does not necessarily indicate a data feed or mapping issue as equipment tends
     * to 'flick through' aspects
     * @see <a href="https://wiki.openraildata.com/index.php?title=Signalling_Nomenclature">Signalling Nomenclature</a>
     */
    public static final int NEITHER = 2;
    
    /**
     * Used when the normal (NK) and reverse (RK) are both 1. IE: The point is both normal and reverse.
     * The use of this does not necessarily indicate a data feed or mapping issue as equipment tends
     * to 'flick through' aspects
     * @see <a href="https://wiki.openraildata.com/index.php?title=Signalling_Nomenclature">Signalling Nomenclature</a>
     */
    public static final int BOTH = 3;
    
    /**
     * Returns a string representation of the point state <br>
     * Eg: {@code point.translateState(}{@link Point#REVERSE Point.REVERSE})
     * returns "Reverse"
     * @param state constant representing the point state
     * @return string representation of equipment state
     */
    public static String translateState(int state) {
         return switch (state) {
             case NORMAL -> "Normal";
             case REVERSE -> "Reverse";
             case NEITHER -> "Neither";
             case BOTH -> "Both";
             default -> throw new IllegalArgumentException(state + " is not a valid Point state");
        };
    }

}
