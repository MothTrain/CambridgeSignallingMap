package aradnezami.cambridgesignallingmap.DiagramElements;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;

/**
 * An element collection is used to have all elements in a diagram held in one data structure. The collection
 * also supports searching for elements as well as drawing all the elements on a given graphics context. Elements
 * in the collection cannot be overwritten after construction
 *
 * <h3>Drawing</h3>
 * When the {@link #draw(Graphics2D)} method is invoked, elements are rendered onto the Graphics context. In the
 * event that elements overlap, the element with the highest priority will be displayed on top. The list of priority
 * is as follows: <br>
 * <br>
 * <i>Highest Priority</i>
 * <li>{@link Signal}s</li>
 * <li>{@link Berth}s</li>
 * <li>{@link Track}s</li>
 * <li>{@link Text}</li>
 * <li>{@link Rectangle}s</li>
 * <i>Lowest Priority</i> <br>
 *  <br>
 * Note that points, routes and trackCircuits are not renderable elements. Instead they modify renderable elements
 */
public class ElementCollection {
    // real elements
    private final @NotNull HashMap<String, Track> tracks;
    private final @NotNull HashMap<String, Signal> signals;
    private final @NotNull HashMap<String, Berth> berths;
    private final @NotNull HashMap<String, Text> texts;
    private final @NotNull HashMap<String, Rectangle> rectangles;

    // Undrawn elements
    private final @NotNull HashMap<String, Point> points;
    private final @NotNull HashMap<String, Route> routes;
    private final @NotNull HashMap<String, TrackCircuit> trackCircuits;


    /**
     * Creates an element collection with the following Elements
     */
    public ElementCollection(@NotNull HashMap<String, Track> tracks,
                             @NotNull HashMap<String, Signal> signals,
                             @NotNull HashMap<String, Berth> berths,
                             @NotNull HashMap<String, Text> texts,
                             @NotNull HashMap<String, Rectangle> rectangles,
                             @NotNull HashMap<String, Point> points,
                             @NotNull HashMap<String, Route> routes,
                             @NotNull HashMap<String, TrackCircuit> trackCircuits) {

        this.tracks = tracks;
        this.signals = signals;
        this.berths = berths;
        this.texts = texts;
        this.rectangles = rectangles;
        this.points = points;
        this.routes = routes;
        this.trackCircuits = trackCircuits;
    }


    /**
     * Draws all renderable elements on the graphics context. In the event that elements overlap, the element
     * with the highest priority will be displayed on top. The list of priority is as follows: <br>
     * <br>
     * <i>Highest Priority</i>
     * <li>{@link Signal}s</li>
     * <li>{@link Berth}s</li>
     * <li>{@link Track}s</li>
     * <li>{@link Text}</li>
     * <li>{@link Rectangle}s</li>
     * <i>Lowest Priority</i> <br>
     *  <br>
     * Note that points, routes and trackCircuits are not renderable elements. Instead they modify renderable elements
     * @param g2d The graphics context to draw on
     */
    public void draw(Graphics2D g2d) {
        for (Rectangle rectangle : rectangles.values()) {
            rectangle.draw(g2d);
        }

        for (Text text : texts.values()) {
            text.draw(g2d);
        }

        for (Track track : tracks.values()) {
            track.draw(g2d);
        }

        for (Berth berth : berths.values()) {
            berth.draw(g2d);
        }

        for (Signal signal : signals.values()) {
            signal.draw(g2d);
        }
    }


    public Signal getSignal(String name) {
        return signals.get(name);
    }
    public Berth getBerth(String name) {
        return berths.get(name);
    }
    public Point getPoint(String name) {
        return points.get(name);
    }
    public Route getRoute(String name) {
        return routes.get(name);
    }
    public TrackCircuit getTrackCircuit(String name) {
        return trackCircuits.get(name);
    }
}
