package aradnezami.cambridgesignallingmap.UI.DiagramElements;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * The route class holds a series of {@link Track}s and sets them as routed when called to do so. The
 * tracks may be modified after construction of the route, however if any tracks are overwritten, the
 * {@link #setTracks(Track[])} method must be used. Note that any changes to state will not apply until
 * the Track's {@link Track#draw(Graphics2D)} method is called
 */
public class Route {
    public static final int MAIN_TYPE = 4;
    public static final int SHUNT_TYPE = 5;
    public static final int CALL_ON_TYPE = 6;
    
    public static final int SET = 0;
    public static final int NOTSET = 1;

    @NotNull
    public String name;

    private Track[] tracks;

    private boolean isRouted;


    /**
     * Creates a route with the following details
     * @param name Unique name of the route
     * @param tracks The tracks covered by the route
     */
    public Route(@NotNull String name, Track[] tracks) {
        this.name = name;
        this.tracks = tracks;
    }


    /**
     * Calls {@link Track#setRouted(boolean, Route)} on the tracks stored by this route
     * @param state The state of the route. {@link #SET} or {@link #NOTSET}
     */
    public void setState(@MagicConstant(intValues = {0,1}) int state) {
        for (Track track : tracks) {
            isRouted = state == SET;
            track.setRouted(isRouted, this);
        }
    }

    /**
     * Not to be used by a non diagram editor
     */
    public void setTracks(Track[] tracks) {
        for (Track track : this.tracks) { // Return any tracks being removed to normal state
            track.setRouted(false, this);
        }

        this.tracks = tracks;

        for (Track track : this.tracks) { // Ensure added tracks are in the correct state
            track.setRouted(isRouted, this);
        }
    }

    public Track[] getTracks() {
        return tracks;
    }


    /**
     * Returns a human readable string representation of the provided route state
     * @param state The state of the route. {@link #SET} or {@link #NOTSET}
     * @return A string form of the state
     */
    public static String translateState(int state) {
        return switch (state) {
            case SET -> "Set";
            case NOTSET -> "Not Set";
            default -> throw new IllegalArgumentException(state + " is not a valid Route state");
        };
    }
}
