package aradnezami.cambridgesignallingmap.DiagramElements;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.HashSet;
import java.util.Set;


/**
 * Instances of the Track class are used to conveniently draw track representations on a diagram, using
 * a provided {@link Graphics2D} object, relating to the context being drawn on. Some properties of the track
 * can be altered after construction, however only to the extent that would be required by a live diagram
 * (eg: offsetting and track occupation).
 * <h2>Geometry</h2>
 * <h3>Co-ordinates</h3>
 * The coordinates of the Track ends should not be modified post construction, unless it is by a diagram
 * editing program. The track has 2 coordinates associated
 * with it. The A coordinate and the B coordinate. <b>The 'A' coordinate must always be on the left side</b>
 * Coordinates must also positioned so that the gradient of a line connecting the coordinates
 * is 0, -1 or 1.<br>
 * <h3>Breaks</h3>
 * The end of a track can be moved away from the specified coordinate using a break. A {@link #TC_BREAK}
 * (Track Circuit break) is the only currently supported break currently. For no break, use {@link #NO_BREAK}.
 * Breaks should not be changed post construction, unless it is by a diagram editing program.
 * <h3>Ends</h3>
 * If <b>(and only if)</b> a track's gradient is not 0, the end may be horizontal or vertical. That is: the edge terminating
 * the track at each end can be vertical or horizontal. This is specified by {@link #HORIZONTAL_END} and
 * {@link #VERTICAL_END}. This is modified by{@link #setA_End(int)} and {@link #setB_End(int)}.
 * This can be modified post construction by any user.
 * <h3>Offset</h3>
 * On top of the offset caused by a break (if applicable), the track can be further offset. This is meant
 * to be used with negative values, however positive values will also function. This is modified by
 * {@link #setA_Offset(int)} and {@link #setB_Offset(int)} <br>
 * <br>
 *
 * <h2>Rendering</h2>
 * Note that because the object cannot retain the Graphics context to draw on, the user must override the
 * {@link JComponent#paintComponent(Graphics)} method of the container of the Track, to call this object's
 * {@link #draw(Graphics2D)} method. The user must also ensure that, to apply any changes they have made, they
 * call their container's {@link JComponent#repaint()} method.
 */
public class Track {
    //Constants
    private static final double SCALE = 10;
    private static final int RELATIVE_TRACK_WIDTH = 2;
    private static final int RELATIVE_TC_BREAK_WIDTH = 1;

    private static final Color DEFAULT_COLOUR = new Color(100, 100, 100);
    private static final Color OCCUPIED_COLOUR = new Color(200, 0, 0);
    private static final Color ROUTED_COLOUR = new Color(220, 220, 220);

    public static final int HORIZONTAL_END = 0;
    public static final int VERTICAL_END = 1;

    public static final int NO_BREAK = 2;
    public static final int TC_BREAK = 3;

    @NotNull
    public final String name;
    @NotNull
    public final String datumName;

    //State
    private boolean isOccupied = false;
    private final Set<Route> routesSet = new HashSet<>();

    //Geometry
    @MagicConstant(intValues = {-1,0,1})
    private int gradient;


    private int Ax;
    private int Ay;
    private int A_Offset = 0;
    @MagicConstant(intValues = {0,1})
    private int A_End;
    @MagicConstant(intValues = {2,3})
    private int A_Break;


    private int Bx;
    private int By;
    private int B_Offset = 0;
    @MagicConstant(intValues = {0,1})
    private int B_End;
    @MagicConstant(intValues = {2,3})
    private int  B_Break;


    /**
     * Creates a track with the given properties.
     *
     * <h3>Co-ordinates</h3>
     * The track has 2 coordinates associated with it. The A coordinate and the B coordinate. <b>The 'A' coordinate
     * must always be on the left side</b> Coordinates must also positioned so that the gradient of a line
     * connecting the coordinates is 0, -1 or 1.<br>
     * <h3>Breaks</h3>
     * The end of a track can be moved away from the specified coordinate using a break. A {@link #TC_BREAK}
     * (Track Circuit break) is the only currently supported break currently. For no break, use {@link #NO_BREAK}.
     * <h3>Ends</h3>
     * If <b>(and only if)</b> a track's gradient is not 0, the end may be horizontal or vertical. That is: the edge terminating
     * the track at each end can be vertical or horizontal. This is specified by {@link #HORIZONTAL_END} and
     * {@link #VERTICAL_END}.
     *
     * @param name The unique name of this track
     * @param A_x X-coordinate of the 'A' point
     * @param A_y Y-coordinate of the 'A' point
     * @param B_x X-coordinate of the 'B' point
     * @param B_y Y-coordinate of the 'B' point
     * @param A_End End type of 'A' point
     * @param A_Break Break type of 'A' point
     * @param B_End End type of 'B' point
     * @param B_Break Break type of 'B' point
     *
     * @throws IllegalArgumentException If a validation rule was broken. Rules are stated above
     */
    public Track(@NotNull String name,
                 @NotNull String datumName,
                 int A_x,
                 int A_y,
                 int B_x,
                 int B_y,
                 @MagicConstant(intValues = {0,1}) int A_End,
                 @MagicConstant(intValues = {2,3}) int A_Break,
                 @MagicConstant(intValues = {0,1}) int B_End,
                 @MagicConstant(intValues = {2,3}) int B_Break) {


        if (A_x > B_x) {
            throw new IllegalArgumentException("The x of the 'A' end must be less than the 'B' end: 'A' end must be on the left. " +
                    "Track name="+name + "A_x="+A_x + ", B_x="+B_x);
        }

        double gradient = (double) (A_y - B_y) / (A_x - B_x);
        if (gradient != -1 && gradient != 0 && gradient != 1) {
            throw new IllegalArgumentException("The gradient of the track must be -1,0 or 1. Not: " + gradient +
                    " Track name="+name + ". A_x="+A_x + " A_y="+A_y + " B_x="+B_x + " B_y="+B_y);
        }

        if (gradient==0 && A_End !=VERTICAL_END) {
            throw new IllegalArgumentException("The A end orientation must be vertical if the track is horizontal" +
                    "Track name="+name);
        }
        if (gradient==0 && B_End !=VERTICAL_END) {
            throw new IllegalArgumentException("The B end orientation must be vertical if the track is horizontal" +
                    "Track name="+name);
        }

        this.name = name;
        this.datumName = datumName;

        //noinspection MagicConstant . Always -1,0,1 given the above if statement
        this.gradient = (int) gradient;

        this.Ax = A_x;
        this.Ay = A_y;
        this.A_End = A_End;
        this.A_Break = A_Break;

        this.Bx = B_x;
        this.By = B_y;
        this.B_End = B_End;
        this.B_Break = B_Break;
    }


    /**
     * Draws the track on the provided graphics object
     * @param g2d The graphics object to draw on
     */
    public void draw(Graphics2D g2d) {
        java.awt.Point[] points = new java.awt.Point[4];
        if (A_End == VERTICAL_END) {
            points[0] = new java.awt.Point(Ax, Ay+RELATIVE_TRACK_WIDTH);
            points[1] = new java.awt.Point(Ax, Ay-RELATIVE_TRACK_WIDTH);
        } else if (A_End == HORIZONTAL_END) {
            points[0] = new java.awt.Point(Ax-RELATIVE_TRACK_WIDTH, Ay);
            points[1] = new java.awt.Point(Ax+RELATIVE_TRACK_WIDTH, Ay);
        }
        if (B_End == VERTICAL_END) {
            points[2] = new java.awt.Point(Bx, By-RELATIVE_TRACK_WIDTH);
            points[3] = new java.awt.Point(Bx, By+RELATIVE_TRACK_WIDTH);
        } else if (B_End == HORIZONTAL_END) {
            points[2] = new java.awt.Point(Bx+RELATIVE_TRACK_WIDTH, By);
            points[3] = new java.awt.Point(Bx-RELATIVE_TRACK_WIDTH, By);
        }

        applyBreaks(points, A_Break, B_Break);
        applyOffset(points, A_Offset, B_Offset, gradient);
        scale(points, SCALE);

        if (isOccupied) {
            g2d.setColor(OCCUPIED_COLOUR);
        } else if (!routesSet.isEmpty()) {
            g2d.setColor(ROUTED_COLOUR);
        } else {
            g2d.setColor(DEFAULT_COLOUR);
        }

        Path2D path = new Path2D.Double();
        path.moveTo(points[0].x, points[0].y);
        for (int i = 1; i < 4; i++) {
            path.lineTo(points[i].x, points[i].y);
        }
        path.closePath();
        g2d.fill(path);
    }


    public void setOccupied(boolean occupied) {
        this.isOccupied = occupied;
    }

    /**
     * Adds or removes a route set on the track. If there is one or more route set on the track, the
     * track is considered routed. It is unrouted otherwise. An instance of the route being set is
     * required to be provided so that in the unlikely event of 2 different routes being set over this
     * track, if one route calls this method with routed=false, this track still knows that another
     * route is still set
     * @param routed true if adding a route set, false if removing
     * @param route The route being set
     */
    public void setRouted(boolean routed, Route route) {
        if (routed) {
            this.routesSet.add(route);
        } else {
            this.routesSet.remove(route);
        }
    }


    // A Setters
    public void setA_Offset(int A_Offset) {
        this.A_Offset = A_Offset;
    }
    public void setA_End(int A_End) {
        this.A_End = A_End;
    }
    public void setA_Break(int A_Break) {
        this.A_Break = A_Break;
    }
    // A Getters
    public int getAy() {
        return Ay;
    }
    public int getAx() {
        return Ax;
    }
    public int getA_Break() {
        return A_Break;
    }
    public int getA_End() {
        return A_End;
    }

    // B setters
    public void setB_Offset(int B_Offset) {
        this.B_Offset = B_Offset;
    }
    public void setB_End(int B_End) {
        if (gradient==0 && A_End !=VERTICAL_END) {
            throw new IllegalArgumentException("The A end orientation must be vertical if the track is horizontal" +
                    "Track name="+name);
        }

        this.B_End = B_End;
    }
    public void setB_Break(int B_Break) {
        this.B_Break = B_Break;
    }
    // B getters
    public int getBx() {
        return Bx;
    }
    public int getBy() {
        return By;
    }
    public int getB_End() {
        return B_End;
    }
    public int getB_Break() {
        return B_Break;
    }

    /**
     * Not to be used by a non diagram editor.
     */
    public void setCoordinates(int A_x, int A_y, int B_x, int B_y) {
        if (A_x > B_x) {
            throw new IllegalArgumentException("The x of the 'A' end must be less than the 'B' end: 'A' end must be on the left. " +
                    "Track name="+name + "A_x="+A_x + ", B_x="+B_x);
        }

        double gradient = (double) (A_y - B_y) / (A_x - B_x);
        if (gradient != -1 && gradient != 0 && gradient != 1) {
            throw new IllegalArgumentException("The gradient of the track must be -1,0 or 1. Not: " + gradient +
                    " Track name="+name + ". A_x="+A_x + " A_y="+A_y + " B_x="+B_x + " B_y="+B_y);
        }

        if (gradient==0 && A_End !=VERTICAL_END) {
            throw new IllegalArgumentException("The A end orientation must be vertical if the track is horizontal" +
                    "Track name="+name);
        }
        if (gradient==0 && B_End !=VERTICAL_END) {
            throw new IllegalArgumentException("The B end orientation must be vertical if the track is horizontal" +
                    "Track name="+name);
        }

        //noinspection MagicConstant
        this.gradient = (int) gradient;

        Ax = A_x;
        Ay = A_y;
        Bx = B_x;
        By = B_y;
    }




    public static void main(String[] args) {
        JFrame frame = new JFrame("Overlapping Tracks");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        DrawingPanel panel = new DrawingPanel();

        Track DnBranch1 = track(10,20, 60,20);
        Track DnBranch2 = track(60,20, 110,70);

        Track UpBranch1 = track(10,40, 50,40);
        Track UpBranch2 = track(50,40, 80,70);

        Track DnMain1 = track(10,70, 80,70);
        Track DnMain2 = track(80,70, 90,70);
        Track DnMain3 = track(90,70, 110,70);
        Track DnMain4 = track(110,70, 160,70);

        Track UpMain1 = track(10,90, 110,90);
        Track UpMain2 = track(110,90, 160,90);

        Track crossover = track(90,70, 110,90);


        panel.addQuadrilateral(DnBranch1);
        panel.addQuadrilateral(DnBranch2);
        panel.addQuadrilateral(UpBranch1);
        panel.addQuadrilateral(UpBranch2);
        panel.addQuadrilateral(DnMain1);
        panel.addQuadrilateral(DnMain2);
        panel.addQuadrilateral(DnMain3);
        panel.addQuadrilateral(DnMain4);
        panel.addQuadrilateral(UpMain1);
        panel.addQuadrilateral(UpMain2);
        panel.addQuadrilateral(crossover);

        frame.setSize(600, 600);
        frame.add(panel);
        frame.setVisible(true);


        panel.repaint();



        Scanner sc = new Scanner(System.in);
        while (true) {
            sc.nextLine();

            // 1 Normal
            DnBranch2.B_End = HORIZONTAL_END;
            DnBranch2.B_Offset = -4;
            DnMain3.B_Offset = 0;
            // 2 Normal
            UpBranch2.B_End = HORIZONTAL_END;
            UpBranch2.B_Offset = -4;
            DnMain1.B_Offset = 0;
            // 3 Normal
            DnMain3.A_Offset = 0;
            UpMain1.B_Offset = 0;
            crossover.A_End = HORIZONTAL_END;
            crossover.A_Offset = -4;
            crossover.B_End = HORIZONTAL_END;
            crossover.B_Offset = -4;
            panel.repaint();

            sc.nextLine();

            // 1 Reverse
            DnBranch2.B_End = VERTICAL_END;
            DnBranch2.B_Offset = 0;
            DnMain3.B_Offset = -7;
            panel.repaint();

            sc.nextLine();




    private static java.awt.Point[] scale(java.awt.Point[] points, @MagicConstant double scale) {
        for (java.awt.Point point : points) {
            point.x = (int) Math.ceil(point.x * scale);
            point.y = (int) Math.ceil(point.y * scale);
        }

        return points;
    }

    /**
     * Index 0 and 1 must be 'A' end. Index 2 and 3 must be 'B' end.
     */
    private static java.awt.Point[] applyBreaks(java.awt.Point[] points, int A_BreakType, int B_BreakType) {
        // A end
        if (A_BreakType == TC_BREAK) {
            points[0] = new java.awt.Point(points[0].x+RELATIVE_TC_BREAK_WIDTH, points[0].y);
            points[1] = new java.awt.Point(points[1].x+RELATIVE_TC_BREAK_WIDTH, points[1].y);
        }

        // B end
        if (B_BreakType == TC_BREAK) {
            points[2] = new java.awt.Point(points[2].x - RELATIVE_TC_BREAK_WIDTH, points[2].y);
            points[3] = new java.awt.Point(points[3].x - RELATIVE_TC_BREAK_WIDTH, points[3].y);
        }

        return points;
    }

    /**
     * Index 0 and 1 must be 'A' end. Index 2 and 3 must be 'B' end.
     */
    private static java.awt.Point[] applyOffset(java.awt.Point[] points, int A_Offset, int B_Offset, int gradient) {
        if (gradient == 0) {
            // A end
            points[0] = new java.awt.Point(points[0].x-A_Offset,  points[0].y);
            points[1] = new java.awt.Point(points[1].x-A_Offset,  points[1].y);

            // B end
            points[2] = new java.awt.Point(points[2].x+B_Offset, points[2].y);
            points[3] = new java.awt.Point(points[3].x+B_Offset, points[3].y);
        } else if (gradient == -1) {
            // A end
            points[0] = new java.awt.Point(points[0].x-A_Offset,  points[0].y+A_Offset);
            points[1] = new java.awt.Point(points[1].x-A_Offset,  points[1].y+A_Offset);

            // B end
            points[2] = new java.awt.Point(points[2].x+B_Offset, points[2].y-B_Offset);
            points[3] = new java.awt.Point(points[3].x+B_Offset, points[3].y-B_Offset);
        } else if (gradient == 1) {
            // A end
            points[0] = new java.awt.Point(points[0].x-A_Offset,  points[0].y-A_Offset);
            points[1] = new java.awt.Point(points[1].x-A_Offset,  points[1].y-A_Offset);

            // B end
            points[2] = new java.awt.Point(points[2].x+B_Offset, points[2].y+B_Offset);
            points[3] = new java.awt.Point(points[3].x+B_Offset, points[3].y+B_Offset);
        }

        return points;
    }

    private static Track track(int a, int b, int c, int d) {
        return new Track("", "",a,b,c,d, VERTICAL_END,NO_BREAK, VERTICAL_END, NO_BREAK);
    }
}
