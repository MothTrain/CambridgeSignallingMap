package aradnezami.cambridgesignallingmap.DiagramElements;

import org.jetbrains.annotations.NotNull;

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


    private static final int REVERSE_TRACK_OFFSET = -4;
    private static final int NORMAL_TRACK_OFFSET = -7;


    @NotNull
    public final String name;



    @NotNull
    private final PointEnd[] pointEnds;

    public Point(@NotNull String name, PointEnd[] pointEnds) {
        this.pointEnds = pointEnds;
        this.name = name;
    }

    public void setState(int state) {
        for (PointEnd pointEnd : pointEnds) {
            pointEnd.setState(state);
        }
    }

    public @NotNull PointEnd[] getPointEnds() {
        return pointEnds;
    }


    public static class PointEnd {

        @NotNull
        public final String name;

        @NotNull
        private Track normalTrack;
        private char normalEnd;
        @NotNull
        private Track reverseTrack;
        private char reverseEnd;

        /**
         * Creates a point with the following properties
         *
         * @param name         The name of the point
         * @param normalTrack  The track that is connected when the point is normal
         * @param normalEnd    The end of the track that is associated with the point ('A' or 'B')
         * @param reverseTrack The track that is connected when the point is reverse
         * @param reverseEnd   The end of the track that is associated with the point ('A' or 'B')
         */
        public PointEnd(@NotNull String name,
                     @NotNull Track normalTrack,
                     char normalEnd,
                     @NotNull Track reverseTrack,
                     char reverseEnd) {

            if (normalEnd != 'A' && normalEnd != 'B') {
                throw new IllegalArgumentException("Normal track end must be 'A' or 'B'. End=" + normalEnd +
                        "Track=" + normalTrack + " Point=" + name);
            }
            if (reverseEnd != 'A' && reverseEnd != 'B') {
                throw new IllegalArgumentException("Reverse track end must be 'A' or 'B'. End=" + reverseEnd +
                        "Track=" + reverseTrack + " Point=" + name);
            }

            int normalTrackEnd = (normalEnd == 'A') ? normalTrack.getA_CurrentEnd() : normalTrack.getB_CurrentEnd();
            if (normalTrackEnd != Track.VERTICAL_END) {
                throw new IllegalArgumentException("Normal track must have a vertical end. End=" + normalEnd +
                        " Track=" + normalTrack.name + " Point=" + name);
            }
            int reverseTrackEnd = (reverseEnd == 'A') ? reverseTrack.getA_CurrentEnd() : reverseTrack.getB_CurrentEnd();
            if (reverseTrackEnd != Track.VERTICAL_END) {
                throw new IllegalArgumentException("reverse track must have a vertical end. End=" + reverseEnd +
                        " Track=" + reverseTrack.name + " Point=" + name);
            }

            int normalX = (normalEnd == 'A') ? normalTrack.getAx() : normalTrack.getBx();
            int normalY = (normalEnd == 'A') ? normalTrack.getAy() : normalTrack.getBy();
            int reverseX = (reverseEnd == 'A') ? reverseTrack.getAx() : reverseTrack.getBx();
            int reverseY = (reverseEnd == 'A') ? reverseTrack.getAy() : reverseTrack.getBy();

            if (normalX != reverseX || normalY != reverseY) {
                throw new IllegalArgumentException("Normal track end and reverse track end must have the" +
                        " same coordinates. Point=" + name + ", Normal Track=" + normalTrack.name + ", Reverse Track=" + reverseTrack.name);
            }


            this.normalEnd = normalEnd;
            this.reverseEnd = reverseEnd;

            this.normalTrack = normalTrack;
            this.reverseTrack = reverseTrack;

            this.name = name;
        }


        /**
         * Sets the state of the point
         *
         * @throws IllegalStateException If an invalid state is provided
         */
        public void setState(int state) {
            if (state == NORMAL) {
                modifyNormalTrack(0);
                modifyReverseTrack(REVERSE_TRACK_OFFSET, Track.HORIZONTAL_END);
            } else if (state == REVERSE) {
                modifyNormalTrack(NORMAL_TRACK_OFFSET);
                modifyReverseTrack(0, Track.VERTICAL_END);
            } else if (state == BOTH) {
                modifyNormalTrack(0);
                modifyReverseTrack(0, Track.VERTICAL_END);
            } else if (state == NEITHER) {
                modifyNormalTrack(NORMAL_TRACK_OFFSET);
                modifyReverseTrack(REVERSE_TRACK_OFFSET, Track.HORIZONTAL_END);
            } else {
                throw new IllegalArgumentException("Unknown point state=" + state + " Track");
            }
        }



    public @NotNull Track getReverseTrack() {
        return reverseTrack;
    }
    public char getReverseEnd() {
        return reverseEnd;
    }
    /**
     * Not to be used by a non diagram editor. Resets the previous reverse track to its original state
     * and replaces it with the provided track.
     * @throws IllegalStateException If the track's end is not vertical
     */
    public void setReverseTrack(@NotNull Track track, char reverseEnd) {
        int reverseTrackEnd = (reverseEnd=='A')? track.getA_CurrentEnd() : track.getB_CurrentEnd();
        if (reverseTrackEnd != Track.VERTICAL_END) {
            throw new IllegalArgumentException("Reverse track must have a vertical end. End="+reverseEnd +
                    " Track=" +track.name + " Point="+name);
        }

        if (this.reverseEnd=='A') { // Return to defaults
            reverseTrack.setA_CurrentEnd(track.getA_DefaultEnd());
            reverseTrack.setA_Offset(0);
        } else {
            reverseTrack.setB_CurrentEnd(track.getB_DefaultEnd());
            reverseTrack.setB_Offset(0);
        }

        this.reverseEnd = reverseEnd;
        this.reverseTrack = track;
    }


    public @NotNull Track getNormalTrack() {
        return normalTrack;
    }
    public char getNormalEnd() {
        return normalEnd;
    }
    /**
     * Not to be used by a non diagram editor. Resets the previous normal track to its original state
     * and replaces it with the provided track
     * @throws IllegalArgumentException If the track's end is not vertical
     */
    public void setNormalTrack(@NotNull Track track, char normalEnd) {
        int normalTrackEnd = (normalEnd=='A')? track.getA_CurrentEnd() : track.getB_CurrentEnd();
        if (normalTrackEnd != Track.VERTICAL_END) {
            throw new IllegalArgumentException("Normal track must have a vertical end. End="+normalEnd +
                    " Track=" +track.name + " Point="+name);
        }

        if (this.normalEnd=='A') {
            normalTrack.setA_CurrentEnd(reverseTrack.getA_DefaultEnd());
            normalTrack.setA_Offset(0);
        } else {
            normalTrack.setB_CurrentEnd(reverseTrack.getB_DefaultEnd());
            normalTrack.setB_Offset(0);
        }

        this.normalEnd = normalEnd;
        this.normalTrack = track;
    }



    private void modifyNormalTrack(int offset) {
        if (normalEnd == 'A') {
            normalTrack.setA_Offset(offset);
        } else {
            normalTrack.setB_Offset(offset);
        }
    }

        private void modifyReverseTrack(int offset, int endType) {
            if (reverseEnd == 'A') {
                reverseTrack.setA_Offset(offset);
                reverseTrack.setA_CurrentEnd(endType);
            } else {
                reverseTrack.setB_Offset(offset);
                reverseTrack.setB_CurrentEnd(endType);
            }
        }
    }

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
