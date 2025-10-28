package aradnezami.cambridgesignallingmap.UI.DiagramElements;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * A track circuit holds a series of tracks and sets them as occupied or unoccupied . The
 * tracks may be modified after construction of the route, however if any tracks are overwritten, the
 * {@link #setTracks(Track[])} method must be used. Note that any changes to state will not apply until
 * the Track's {@link Track#draw(Graphics2D)} method is called
 */
public class TrackCircuit {
    /**
     * Used to indicate the event type
     */
    public static final int TYPE = 2;
    
    public static final int UNOCCUPIED = 0;
    public static final int OCCUPIED = 1;

    @NotNull
    public String name;

    @NotNull
    private Track[] tracks;
    private boolean isOccupied;

    /**
     * Creates a track circuit with the following properties
     * @param name Unique name of the track circuit
     * @param tracks The tracks covered by the track circuit
     */
    public TrackCircuit(@NotNull String name,
                        @NotNull Track[] tracks) {

        this.name = name;
        this.tracks = tracks;
        isOccupied = false;
    }

    public void setState(int state) {
        switch (state) {
            case UNOCCUPIED -> isOccupied = false;
            case OCCUPIED -> isOccupied = true;
            default -> throw new IllegalStateException("Unknown track circuit state: " + state + " TC=" + name);
        }

        for (Track track : tracks) {
            track.setOccupied(isOccupied);
        }
    }

    public Track[] getTracks() {
        return tracks;
    }
    public void setTracks(Track[] tracks) {
        this.tracks = tracks;
    }
    
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
