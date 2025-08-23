package aradnezami.cambridgesignallingmap.DiagramElements;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
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
 * <li>{@link Text}</li>
 * <li>{@link Track}s</li>
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

    private final @NotNull HashMap<String, Rectangle> rectangles;
    private final @NotNull ArrayList<Text> texts;
    // Undrawn elements

    private final @NotNull HashMap<String, Point> points;
    private final @NotNull HashMap<String, Route> routes;
    private final @NotNull HashMap<String, TrackCircuit> trackCircuits;

    public static double scale = 1;

    /**
     * Creates an element collection with the following Elements
     */
    public ElementCollection(@NotNull ArrayList<Track> tracks,
                             @NotNull ArrayList<Signal> signals,
                             @NotNull ArrayList<Berth> berths,
                             @NotNull ArrayList<Text> texts,
                             @NotNull ArrayList<Rectangle> rectangles,
                             @NotNull ArrayList<Point> points,
                             @NotNull ArrayList<Route> routes,
                             @NotNull ArrayList<TrackCircuit> trackCircuits) {

        this.tracks = new HashMap<>() {{
            for (Track track : tracks) {
                put(track.name, track);
            }
        }};

        this.signals = new HashMap<>() {{
            for (Signal signal : signals) {
                put(signal.name, signal);
            }
        }};

        this.berths = new HashMap<>() {{
            for (Berth berth : berths) {
                put(berth.name, berth);
            }
        }};

        this.texts = texts;

        this.rectangles = new HashMap<>() {{
            for (Rectangle rectangle : rectangles) {
                put(rectangle.name, rectangle);
            }
        }};

        this.points = new HashMap<>() {{
            for (Point point : points) {
                put(point.name, point);
            }
        }};

        this.routes = new HashMap<>() {{
            for (Route route : routes) {
                put(route.name, route);
            }
        }};

        this.trackCircuits = new HashMap<>() {{
            for (TrackCircuit trackCircuit : trackCircuits) {
                put(trackCircuit.name, trackCircuit);
            }
        }};
    }


    /**
     * Draws all renderable elements on the graphics context. In the event that elements overlap, the element
     * with the highest priority will be displayed on top. The list of priority is as follows: <br>
     * <br>
     * <i>Highest Priority</i>
     * <li>{@link Signal}s</li>
     * <li>{@link Berth}s</li>
     * <li>{@link Text}</li>
     * <li>{@link Track}s</li>
     * <li>{@link Rectangle}s</li>
     * <i>Lowest Priority</i> <br>
     *  <br>
     * Note that points, routes and trackCircuits are not renderable elements. Instead they modify renderable elements
     * @param g2d The graphics context to draw on
     */
    public void draw(Graphics2D g2d) {
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(
                RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        for (Rectangle rectangle : rectangles.values()) {
            rectangle.draw(g2d);
        }

        for (Track track : tracks.values()) {
            track.draw(g2d);
        }

        for (Text text : texts) {
            text.draw(g2d);
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


    public @NotNull HashMap<String, Track> getTracks() {
        return tracks;
    }
    public @NotNull HashMap<String, Signal> getSignals() {
        return signals;
    }
    public @NotNull HashMap<String, Berth> getBerths() {
        return berths;
    }
    public @NotNull ArrayList<Text> getTexts() {
        return texts;
    }
    public @NotNull HashMap<String, Rectangle> getRectangles() {
        return rectangles;
    }
    public @NotNull HashMap<String, Point> getPoints() {
        return points;
    }
    public @NotNull HashMap<String, Route> getRoutes() {
        return routes;
    }
    public @NotNull HashMap<String, TrackCircuit> getTrackCircuits() {
        return trackCircuits;
    }


    /**
     * Creates a {@link Dimension} containing the maximum x and y value of any element in
     * the collection. Note: this method does not account for parts of an element that may
     * extent past its x or y value (eg: text)
     * @return A dimension containing the preferred size of a panel displaying this element
     * collection
     */
    public Dimension getSize() {
        Dimension size = new Dimension();

        for (Track track : tracks.values()) {
            if (track.getBx() > size.width) {
                size.width = track.getBx();
            }
            if (track.getAy() > size.height) {
                size.height = track.getAy();
            }
            if (track.getBy() > size.height) {
                size.height = track.getBy();
            }
        }

        for (Signal signal : signals.values()) {
            if (signal.x > size.width) {
                size.width = signal.x;
            }
            if (signal.y > size.height) {
                size.height = signal.y;
            }
        }

        for (Rectangle rectangle : rectangles.values()) {
            if (rectangle.B_x > size.width) {
                size.width = rectangle.B_x;
            }
            if (rectangle.B_y > size.height) {
                size.height = rectangle.B_y;
            }
        }

        for (Berth berth : berths.values()) {
            if (berth.x > size.width) {
                size.width = berth.x;
            }
            if (berth.y > size.height) {
                size.height = berth.y;
            }
        }

        for (Text text : texts) {
            if (text.x > size.width) {
                size.width = text.x;
            }
            if (text.y > size.height) {
                size.height = text.y;
            }
        }

        size.height += 50;
        size.width += 50;
        size.height *= scale;
        size.width *= scale;


        return size;
    }
}
