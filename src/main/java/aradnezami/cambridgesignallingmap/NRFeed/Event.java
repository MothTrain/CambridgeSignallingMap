package aradnezami.cambridgesignallingmap.NRFeed;

import aradnezami.cambridgesignallingmap.DiagramElements.*;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


/**
 * Represents either an S or C-Class event after it has (in the case of S-Class) been decoded into the
 * real equipment it affects. However, an Event is not guaranteed to correspond to valid equipment</br>
 * As this class can store both S and C class events, any attributes that
 * are not common to both, will be null when the event is not of the attribute's corresponding type.
 * This is a compromise made to allow for methods to return any type of event through one return value. </br>
 * If {@link #type} is 'C', {@link #S_Type}, {@link #S_State} and {@link #S_Id} will be null. If the
 * type is 'S', {@link #C_FromBerth}, {@link #C_ToBerth} and {@link #C_Describer} will be null.
 */
public class Event {
    /**
     * The type of event represented. Value is 'C' or 'S'
     * */
    public final char type;
    /**
     * Value can be 0 or above for normal timestamps. If event is not timestamped, value is -1
     */
    public final long timestamp;


    /**
     * The type of signalling equipment which the event applies to.
     * Eg: {@link aradnezami.cambridgesignallingmap.DiagramElements.TrackCircuit#TYPE TrackCircuit.TYPE},
     * or null if the message is C and not S class
     */
    @Nullable @MagicConstant
    public final Integer S_Type;
    /**
     * The new state of the signalling equipment.
     * Eg: {@link aradnezami.cambridgesignallingmap.DiagramElements.TrackCircuit#OCCUPIED TrackCircuit.OCCUPIED},
     * or null if the message is C and not S class
     */
    @Nullable @MagicConstant
    public final Integer S_State;
    /**
     * The name of the signalling equipment to update or null if the message is C and not S class.
     */
    @Nullable
    public final String S_Id;


    /**
     * The berth that the train describer is moving from. {@code NONE} if there is no from berth
     * and null if the event is S and not C class
     */
    @Nullable
    public final String C_FromBerth;
    /**
     * The berth that the train describer is moving to. {@code NONE} if there is no to berth
     * and null if the event is S and not C class
     */
    @Nullable
    public final String C_ToBerth;
    /**
     * The train describer
     */
    @Nullable
    public final String C_Describer;


    /**
     * Creates an S-Class event. The values of {@link #C_FromBerth}, {@link #C_ToBerth} and {@link #C_Describer}
     * will subsequently be null
     *
     * @param timestamp The timestamp provided by the feed, or -1 for no timestamp
     * @param type The type of signalling equipment affected (See {@link #S_Type})
     * @param state The state of signalling equipment affected (See {@link #S_State}
     * @param id The name of equipment affected
     */
    public Event(long timestamp, @MagicConstant int type, @MagicConstant int state, @NotNull String id) {
        this.type = 'S';

        if (timestamp < -1) {throw new IllegalArgumentException("timestamp must not be below -1");}
        this.timestamp = timestamp;

        this.S_Type = type;
        this.S_State = state;
        this.S_Id = id;

        C_FromBerth = null;
        C_ToBerth = null;
        C_Describer = null;
    }

    /**
     * Creates a C-Class event. The values of {@link #S_Type}, {@link #S_State} and {@link #S_Id} will
     * subsequently be null
     *
     * @param timestamp The timestamp provided by the feed, or -1 for no timestamp
     * @param fromBerth The berth that the train describer is moving from. {@code NONE} if there is no from berth
     * @param toBerth The berth that the train describer is moving to. {@code NONE} if there is no from berth
     * @param describer The train describer
     */
    public Event(long timestamp, @NotNull String fromBerth, @NotNull String toBerth, @NotNull String describer) {
        this.type = 'C';

        if (timestamp < -1) {throw new IllegalArgumentException("timestamp must not be below -1");}
        this.timestamp = timestamp;

        C_FromBerth = fromBerth;
        C_ToBerth = toBerth;
        C_Describer = describer;

        this.S_Type = null;
        this.S_State = null;
        this.S_Id = null;
    }


    @Override
    public String toString() {
        if (type == 'C') {
            return "Event C-Class From:" + C_FromBerth + " To:" + C_ToBerth + " Descr:" + C_Describer;
        } else {
            //noinspection DataFlowIssue
            return "Event S-Class "  + switch (S_Type) {
                case (Signal.ASPECT_TYPE) -> "Signal: " + S_Id + " State: " + Signal.translateAspectState(S_State);
                case (Point.TYPE) -> "Point: " + S_Id + " State: " + Point.translateState(S_State);
                case (TrackCircuit.TYPE) -> "Track Circuit: " + S_Id + " State: " + TrackCircuit.translateState(S_State);
                case (Signal.ROUTED_TYPE) -> "Signal Route: " + S_Id + " State: " + Signal.translateRoutedState(S_State);
                case (Route.MAIN_TYPE) -> "Main Route: " + S_Id + " State: " + Route.translateState(S_State);
                case (Route.SHUNT_TYPE) -> "Shunt Route: " + S_Id + " State: " + Route.translateState(S_State);
                case (Route.CALL_ON_TYPE) -> "Call-On Route: " + S_Id + " State: " + Route.translateState(S_State);
                default -> "Unknown Type: " + S_Type + " Id: " + S_Id + " State: " + S_State;
            };
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return type == event.type && timestamp == event.timestamp && Objects.equals(S_Type, event.S_Type) && Objects.equals(S_State, event.S_State) && Objects.equals(S_Id, event.S_Id) && Objects.equals(C_FromBerth, event.C_FromBerth) && Objects.equals(C_ToBerth, event.C_ToBerth) && Objects.equals(C_Describer, event.C_Describer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, timestamp, S_Type, S_State, S_Id, C_FromBerth, C_ToBerth, C_Describer);
    }
}
