package aradnezami.cambridgesignallingmap;

import aradnezami.cambridgesignallingmap.DiagramElements.Point;
import aradnezami.cambridgesignallingmap.DiagramElements.Rectangle;
import aradnezami.cambridgesignallingmap.DiagramElements.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides method(s) for decoding diagram files. When loading files the context {@link ClassLoader}
 * is always used and all diagram .json files <b>must</b> be compliant with the diagramSchema.json
 * provided in the resources folder
 */
public class MapLoader {

    private MapLoader() {}


    /**
     * Loads the json diagram from the provided path, using the context {@link ClassLoader},
     * and converts it into an ElementCollection which is returned. The json file provided
     * <b>must</b> be compliant with the diagramSchema.json in the resources folder
     *
     * @param path The path of the JSON diagram file
     * @throws IOException If the diagram file could not be loaded
     * @throws FontLoadingException If the {@link Text} fonts could not be loaded
     * @throws DiagramFormatException If the diagram file was incorrectly formatted
     */
    public static ElementCollection loadMap(String path) throws IOException {
        InputStream mapInputStream = getMapInputStream(path);
        ElementCollection elements = getElementCollection(mapInputStream);
        mapInputStream.close();

        return elements;
    }


    /**
     * Loads the json diagram from the provided {@link InputStream} and converts it
     * into an ElementCollection which is returned. The json file provided
     * <b>must</b> be compliant with the diagramSchema.json in the resources folder.
     * This method does not close the inputStream upon completion
     *
     * @param in The inputStream relating to the diagram file
     * @throws IOException If the diagram file could not be loaded
     * @throws FontLoadingException If the {@link Text} fonts could not be loaded
     * @throws DiagramFormatException If the diagram file was incorrectly formatted
     */
    public static ElementCollection loadMap(InputStream in) throws IOException {
        return getElementCollection(in);
    }


    @NotNull
    private static ElementCollection getElementCollection(InputStream in) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        POJORoot root = mapper.readValue(in, POJORoot.class);
        POJODatumPoint pojoElements  = simplifyDatums(root).datumPoints.get(0);

        HashMap<String, Track> trackMap = deriveTrackMap(pojoElements.tracks);

        ArrayList<TrackCircuit> trackCircuits = deriveTrackCircuits(pojoElements.tracks, trackMap);
        ArrayList<Point> points = derivePoints(pojoElements.points, trackMap);
        ArrayList<Route> routes = deriveRoutes(pojoElements.routes, trackMap);
        ArrayList<Signal> signals = readSignals(pojoElements.signals);
        ArrayList<Berth> berths = readBerths(pojoElements.berths);
        ArrayList<Rectangle> rectangles = readRectangles(pojoElements.rectangles);
        ArrayList<Text> texts = readTexts(pojoElements.texts);

        return new ElementCollection(
                new ArrayList<>(trackMap.values()),
                signals,
                berths,
                texts,
                rectangles,
                points,
                routes,
                trackCircuits
        );
    }

    /**
     * Returns a {@link POJORoot} with only one datum point named {@code "onlyDatum"}, which contains
     * all the elements of all the datums in the provided root, with any coordinates offset by the
     * coordinates of their respective datum point
     * @param root A root to simplify
     * @return A simplified root
     */
    private static POJORoot simplifyDatums(POJORoot root) {
        POJORoot newRoot = new POJORoot();
        newRoot.datumPoints = new ArrayList<>() {{
                add(new POJODatumPoint() {{
                    name = "onlyDatum";
                    x = 0;
                    y = 0;

                    tracks = new ArrayList<>();
                    signals = new ArrayList<>();
                    points = new ArrayList<>();
                    routes = new ArrayList<>();
                    berths = new ArrayList<>();
                    rectangles = new ArrayList<>();
                    texts = new ArrayList<>();
                }});
        }};


        // Offset all elements by their datum point's coordinates and assign to newRoot
        for (POJODatumPoint oldDatumPoint : root.datumPoints) {

            ArrayList<POJOTrack> newTracks = new ArrayList<>();
            ArrayList<POJOTrack> oldTracks = oldDatumPoint.tracks;
            for (POJOTrack oldTrack: oldTracks) {
                oldTrack.Ax += oldDatumPoint.x;
                oldTrack.Ay += oldDatumPoint.y;
                oldTrack.Bx += oldDatumPoint.x;
                oldTrack.By += oldDatumPoint.y;

                newTracks.add(oldTrack);
            }
            newRoot.datumPoints.get(0).tracks.addAll(newTracks);


            ArrayList<POJOSignal> newSignals = new ArrayList<>();
            ArrayList<POJOSignal> oldSignals = oldDatumPoint.signals;
            for (POJOSignal oldSignal: oldSignals) {
                oldSignal.x += oldDatumPoint.x;
                oldSignal.y += oldDatumPoint.y;

                newSignals.add(oldSignal);
            }
            newRoot.datumPoints.get(0).signals.addAll(newSignals);


            ArrayList<POJOBerth> newBerths = new ArrayList<>();
            ArrayList<POJOBerth> oldBerths = oldDatumPoint.berths;
            for (POJOBerth oldBerth: oldBerths) {
                oldBerth.x += oldDatumPoint.x;
                oldBerth.y += oldDatumPoint.y;

                newBerths.add(oldBerth);
            }
            newRoot.datumPoints.get(0).berths.addAll(newBerths);


            ArrayList<POJORectangle> newRectangles = new ArrayList<>();
            ArrayList<POJORectangle> oldRectangles = oldDatumPoint.rectangles;
            for (POJORectangle oldRectangle: oldRectangles) {
                oldRectangle.Ax += oldDatumPoint.x;
                oldRectangle.Ay += oldDatumPoint.y;
                oldRectangle.Bx += oldDatumPoint.x;
                oldRectangle.By += oldDatumPoint.y;

                newRectangles.add(oldRectangle);
            }
            newRoot.datumPoints.get(0).rectangles.addAll(newRectangles);


            ArrayList<POJOText> newTexts = new ArrayList<>();
            ArrayList<POJOText> oldTexts = oldDatumPoint.texts;
            for (POJOText oldText: oldTexts) {
                oldText.x += oldDatumPoint.x;
                oldText.y += oldDatumPoint.y;

                newTexts.add(oldText);
            }
            newRoot.datumPoints.get(0).texts.addAll(newTexts);

            newRoot.datumPoints.get(0).points.addAll(oldDatumPoint.points);
            newRoot.datumPoints.get(0).routes.addAll(oldDatumPoint.routes);
        }

        return newRoot;
    }


    private static HashMap<String, Track> deriveTrackMap(ArrayList<POJOTrack> tracks) {
        HashMap<String, Track> trackMap = new HashMap<>();

        for (POJOTrack POJOtrack: tracks) {
            boolean isTrackCircuited = !POJOtrack.TC.equals("NONE");

            int AOrientation = switch (POJOtrack.AOrientation) {
                case "V" -> Track.VERTICAL_END;
                case "H" -> Track.HORIZONTAL_END;
                default -> throw new DiagramFormatException("Unknown Track AOrientation=" + POJOtrack.AOrientation + " Track="+POJOtrack.name);
            };
            int ABreak = switch (POJOtrack.ABreak) {
                case "NONE" -> Track.NO_BREAK;
                case "TC" -> Track.TC_BREAK;
                case "DEFAULT" -> (POJOtrack.TC.equals("NONE")) ? Track.NO_BREAK : Track.TC_BREAK;
                default -> throw new DiagramFormatException("Unknown Track ABreak=" + POJOtrack.ABreak + " Track="+POJOtrack.name);
            };

            int BOrientation = switch (POJOtrack.BOrientation) {
                case "V" -> Track.VERTICAL_END;
                case "H" -> Track.HORIZONTAL_END;
                default -> throw new DiagramFormatException("Unknown Track BOrientation=" + POJOtrack.BOrientation + " Track="+POJOtrack.name);
            };
            int BBreak = switch (POJOtrack.BBreak) {
                case "NONE" -> Track.NO_BREAK;
                case "TC" -> Track.TC_BREAK;
                case "DEFAULT" -> (POJOtrack.TC.equals("NONE")) ? Track.NO_BREAK : Track.TC_BREAK;
                default -> throw new DiagramFormatException("Unknown Track BBreak=" + POJOtrack.BBreak + " Track="+POJOtrack.name);
            };

            Track track = new Track(POJOtrack.name,
                    isTrackCircuited,
                    POJOtrack.Ax,
                    POJOtrack.Ay,
                    POJOtrack.Bx,
                    POJOtrack.By,
                    AOrientation,
                    ABreak,
                    BOrientation,
                    BBreak);

            trackMap.put(track.name, track);
        }

        return trackMap;
    }


    private static ArrayList<TrackCircuit> deriveTrackCircuits(ArrayList<POJOTrack> tracks, HashMap<String, Track> trackMap) {

        HashMap<String, ArrayList<Track>> trackCircuitMap = new HashMap<>();

        for (POJOTrack pojoTrack : tracks) {
             if (pojoTrack.TC.equals("NONE")) {continue;}

             ArrayList<Track> TCTracks = trackCircuitMap.get(pojoTrack.TC);
             if (TCTracks == null) {
                 TCTracks = new ArrayList<>();
             }
             TCTracks.add(trackMap.get(pojoTrack.name));

             trackCircuitMap.put(pojoTrack.TC, TCTracks);
        }

        ArrayList<TrackCircuit> trackCircuits = new ArrayList<>();
        for (Map.Entry<String, ArrayList<Track>> entry : trackCircuitMap.entrySet()) {
            trackCircuits.add(
                    new TrackCircuit(entry.getKey(), entry.getValue().toArray(new Track[0]))
            );
        }

        return trackCircuits;
    }


    private static ArrayList<Point> derivePoints(ArrayList<POJOPoint> pojoPoints, HashMap<String, Track> trackMap) {
        ArrayList<Point> points = new ArrayList<>();
        for (POJOPoint pojoPoint : pojoPoints) {
            Track normalTrack = trackMap.get(pojoPoint.NTrack);
            if (normalTrack == null) {
                throw new DiagramFormatException("Unknown point normal track=" + pojoPoint.NTrack + " Point=" + pojoPoint.name);
            }
            Track reverseTrack = trackMap.get(pojoPoint.RTrack);
            if (reverseTrack == null) {
                throw new DiagramFormatException("Unknown point reverse track=" + pojoPoint.RTrack + " Point=" + pojoPoint.name);
            }

            points.add(new Point(
                    pojoPoint.name,
                    normalTrack,
                    pojoPoint.end.charAt(0),
                    reverseTrack,
                    pojoPoint.end.charAt(0)
            ));
        }

        return points;
    }


    private static ArrayList<Route> deriveRoutes(ArrayList<POJORoute> pojoRoutes, HashMap<String, Track> trackMap) {
        ArrayList<Route> routes = new ArrayList<>();
        for (POJORoute pojoRoute : pojoRoutes) {

            ArrayList<String> trackNames = pojoRoute.tracks;
            ArrayList<Track> tracks = new ArrayList<>();
            for (String trackName : trackNames) {
                Track track = trackMap.get(trackName);
                if (track == null) {
                    throw new DiagramFormatException("Unknown route track=" + trackName + " route=" + pojoRoute.name);
                }
                tracks.add(track);
            }
            Route route = new Route(
                    pojoRoute.name,
                    tracks.toArray(tracks.toArray(new Track[0]))
            );
            routes.add(route);
        }

        return routes;
    }


    private static ArrayList<Signal> readSignals(ArrayList<POJOSignal> pojoSignals) {
        ArrayList<Signal> signals = new ArrayList<>();
        for (POJOSignal pojoSignal : pojoSignals) {

            int offset = switch (pojoSignal.offset) {
                case "UP" -> Signal.OFFSET_UP;
                case "DN" -> Signal.OFFSET_DOWN;
                default -> throw new DiagramFormatException("Unknown signal offset="+pojoSignal.offset + " signal="+pojoSignal.name);
            };
            int orientation = switch (pojoSignal.orientation) {
                case "L" -> Signal.LEFT;
                case "R" -> Signal.RIGHT;
                default -> throw new DiagramFormatException("Unknown signal orientation="+pojoSignal.orientation + " signal="+pojoSignal.name);
            };

            Signal signal = switch (pojoSignal.form) {
                case "M" -> new MainSignal(pojoSignal.name, pojoSignal.x, pojoSignal.y, offset, orientation);
                case "S" -> new ShuntSignal(pojoSignal.name, pojoSignal.x, pojoSignal.y, offset, orientation);
                case "C" -> new CompoundSignal(pojoSignal.name, pojoSignal.x, pojoSignal.y, offset, orientation);
                default -> throw new DiagramFormatException("Unknown signal form="+pojoSignal.form + " signal="+pojoSignal.name);
            };
            signals.add(signal);
        }
        return signals;
    }


    private static ArrayList<Berth> readBerths(ArrayList<POJOBerth> pojoBerths) {
        ArrayList<Berth> berths = new ArrayList<>();
        for (POJOBerth pojoBerth : pojoBerths) {
            berths.add(new Berth(
                    pojoBerth.name,
                    pojoBerth.x,
                    pojoBerth.y
                    )
            );
        }

        return berths;
    }


    private static ArrayList<Rectangle> readRectangles(ArrayList<POJORectangle> pojoRectangles) throws FontLoadingException {
        ArrayList<Rectangle> rectangles = new ArrayList<>();
        for (POJORectangle pojoRectangle: pojoRectangles) {

            Color colour;
            if (pojoRectangle.colour.equals("PLAT")) {
                colour = Rectangle.PLATFORM_COLOR;
            } else {
                colour = getColor(pojoRectangle.colour);
            }

            rectangles.add(new Rectangle(
                    pojoRectangle.name,
                    pojoRectangle.Ax,
                    pojoRectangle.Ay,
                    pojoRectangle.Bx,
                    pojoRectangle.By,
                    colour
            ));
        }

        return rectangles;
    }


    private static ArrayList<Text> readTexts(ArrayList<POJOText> pojoTexts) throws FontLoadingException {
        ArrayList<Text> texts = new ArrayList<>();
        for (POJOText pojoText : pojoTexts) {
            Color colour = (pojoText.colour.equals("DEFAULT")) ? colour = Text.DEFAULT_COLOUR : getColor(pojoText.colour);

            texts.add(new Text(
                    pojoText.text,
                    pojoText.x,
                    pojoText.y,
                    colour,
                    pojoText.size,
                    Text.GENERAL_FONT
                    )
            );
        }

        return texts;
    }



    private static InputStream getMapInputStream(String path) throws IOException {
        ClassLoader classLoader = MapLoader.class.getClassLoader();
        InputStream mapStream = classLoader.getResourceAsStream(path);
        if (mapStream == null) {
            throw new FileNotFoundException("Could not find diagram map. Path: " + path);
        }

        return mapStream;
    }



    private static @NotNull Color getColor(String str) {
        Color colour;
        if (str.startsWith("#")) {
            str = str.substring(1);
        }

        // Parse the color components
        int r = 0;
        int g = 0;
        int b = 0;
        try {
            r = Integer.valueOf(str.substring(0, 2), 16);
            g = Integer.valueOf(str.substring(2, 4), 16);
            b = Integer.valueOf(str.substring(4, 6), 16);
        } catch (NumberFormatException e) {
            throw new DiagramFormatException("Colour format was invalid. Colour="+str);
        }

        colour = new Color(r, g, b);
        return colour;
    }


    // JSON objects
    private static class POJORoot {
        @JsonProperty("$schema")
        public String schema;

        public ArrayList<POJODatumPoint> datumPoints;
    }

    private static class POJODatumPoint {


        public String name;
        public int x;
        public int y;

        public ArrayList<POJOTrack> tracks;
        public ArrayList<POJOSignal> signals;
        public ArrayList<POJOPoint> points;
        public ArrayList<POJORoute> routes;
        public ArrayList<POJOBerth> berths;
        public ArrayList<POJORectangle> rectangles;
        public ArrayList<POJOText> texts;
    }
    private static class POJOTrack {
        public String name;
        public int Ax;
        public int Ay;
        public String AOrientation = "V";
        public String ABreak = "DEFAULT";

        public int Bx;
        public int By;
        public String BOrientation = "V";
        public String BBreak = "DEFAULT";

        public String TC = "NONE";
    }

    private static class POJOSignal {
        public String name;
        public String form;

        public int x;
        public int y;
        public String orientation;
        public String offset;
    }

    private static class POJOPoint {
        public String name;
        public String NTrack;
        public String RTrack;
        public String end;
    }

    private static class POJORoute {
        public String name;
        public ArrayList<String> tracks;
    }

    private static class POJOBerth {
        public String name;
        public int x;
        public int y;
    }

    private static class POJORectangle {
        public String name;
        public int Ax;
        public int Ay;
        public int Bx;
        public int By;
        public String colour;
    }

    private static class POJOText {
        public String text;
        public int x;
        public int y;
        public int size;
        public String colour;
    }
}
