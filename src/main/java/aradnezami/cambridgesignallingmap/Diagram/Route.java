package aradnezami.cambridgesignallingmap.Diagram;

public class Route {
    public static final int TYPE = 4;
    
    public static final int SET = 0;
    public static final int NOTSET = 1;
    
    public static String translateState(int state) {
        return switch (state) {
            case SET -> "Set";
            case NOTSET -> "Not Set";
            default -> throw new IllegalArgumentException(state + " is not a valid Route state");
        };
    }
}
