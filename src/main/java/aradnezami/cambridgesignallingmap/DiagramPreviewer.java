package aradnezami.cambridgesignallingmap;

import aradnezami.cambridgesignallingmap.DiagramElements.Point;
import aradnezami.cambridgesignallingmap.DiagramElements.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * The DiagramPreviewer is a non-instantiable class used solely for its main method which repeatedly
 * loads the .json diagram located at the path specified by the first command line argument. The
 * previewer also displays labels for tracks, points, signals and berths, which can be toggled on
 * and off using inputs from the standard input stream. <br>
 */
public class DiagramPreviewer {
    public static final String HELP_MSG = """
            The following commands are available. Anything enclosed in brackets are
            arguments and are not optional.
            - trackLabel -> Toggles track labels
            - pointLabel -> Toggles point labels
            - sigLabel -> Toggles signal labels
            - berthLabel -> Toggles berths labels
            
            - sig (signalName) (state) -> Sets the state of the signal (allowed states: off, on, soff, bothoff)
            - sigRoute (signalName) (state) -> Sets routed state of the signal (allowed states: set, notset)
            - point (pointName) (state) -> Sets a point position (allowed states: n, r, both, neither)
            - tc (tcName) (state) -> Sets state of a track circuit (allowed states: occupied, unoccupied)
            - route (routeName) (state) -> Sets the state of the route (allowed states: set, notset)
            
            - scale (newScale) -> Adjusts the display scale to newScale
            - toggleReloads -> Turns repeated automatic off (default on)
            - reload -> Manually reloads the diagram for when automatic reloading is off
            
            - help -> Shows this help message
            """;

    private static boolean tracks = true;
    private static boolean points = true;
    private static boolean signals = true;
    private static boolean berths = true;

    private DiagramPreviewer() {}

    private static String diagramPath;
    private static DiagramPanel diagram;
    private static ElementCollection elements;
    private static final ActionListener reloadMap = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            try (InputStream inputStream = new FileInputStream(diagramPath)) {
                elements = MapLoader.loadMap(inputStream);
                elements = populateDiagram(elements);
                diagram.setElements(elements);
                diagram.repaint();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public static void main(String[] args) {
        diagramPath = args[0];
        ElementCollection.scale = 4;


        try (InputStream inputStream = new FileInputStream(diagramPath)) {
            elements = populateDiagram(MapLoader.loadMap(inputStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        JFrame frame = new JFrame("Diagram Previewer");

        Timer reloadTimer = new Timer(500, reloadMap);
        reloadTimer.setInitialDelay(0);
        SwingUtilities.invokeLater(() -> {
            diagram = new DiagramPanel(elements);

            frame.add(diagram);
            frame.setSize(new Dimension(800, 600));
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setVisible(true);
            reloadTimer.start();
        });

        System.out.println(HELP_MSG);
        Scanner scanner = new Scanner(System.in);

        do {
            String[] command = scanner.nextLine().split(" ");
            switch (command[0].toLowerCase()) {
                case "togglereloads": {
                    if ((reloadTimer.isRunning())) {reloadTimer.stop();
                    } else {reloadTimer.start();}
                } break;
                case "reload": {
                    if (reloadTimer.isRunning()) {
                        System.out.println("Automatic reloads are already on");
                        break;}
                    SwingUtilities.invokeLater(() -> reloadMap.actionPerformed(null));
                } break;
                case "tracklabel": tracks = !tracks; break;
                case "pointlabel": points = !points; break;
                case "siglabel": signals = !signals; break;
                case "berthlabel": berths = !berths; break;
                case "sig": setSignalAspect(command, diagram); break;
                case "sigroute" : setSignalRouted(command, diagram); break;
                case "point": setPoint(command, diagram); break;
                case "tc" : setTrackCircuit(command, diagram); break;
                case "route": setRoute(command, diagram); break;

                case "scale":
                    try {ElementCollection.scale = Double.parseDouble(command[1]); break; }
                    catch (NumberFormatException e) {
                        System.out.println("Not a parsable decimal");
                    }
                    break;
                case "help":
                    System.out.println(HELP_MSG);
                    break;
                default: System.out.println("Not a valid command");
            }
            diagram.repaint();
        } while(frame.isVisible());

    }

    private static void setSignalAspect(String[] command, DiagramPanel diagram) {
        if (command.length != 3) {System.out.println("This command must have 2 arguments"); return;}

        int state = switch (command[2]) {
            case "on" -> Signal.ON;
            case "off" -> Signal.MAIN_OFF;
            case "soff" -> Signal.SHUNT_OFF;
            case "bothoff" -> Signal.BOTH_OFF;
            default -> -1;
        };
        if (state == -1) {System.out.println("\"" + command[2] + "\" is not a valid signal state"); return;}

        try {
            boolean signalExists = diagram.setSignalAspect(command[1], state);
            if (!signalExists) {System.out.println("Signal \"" + command[1] + "\" does not exist");}
        } catch (IllegalArgumentException e) {
            System.out.println("\"" + command[2] + "\" is not a valid signal state for this type of signal");
        }
    }


    private static void setSignalRouted(String[] command, DiagramPanel diagram) {
        if (command.length != 3) {System.out.println("This command must have 2 arguments"); return;}

        int state = switch (command[2]) {
            case "set"-> Signal.ROUTE_SET;
            case "notset" -> Signal.ROUTE_NOT_SET;
            default -> -1;
        };
        if (state == -1) {System.out.println("\"" + command[2] + "\" is not a valid signal routing state"); return;}

        boolean signalExists = diagram.setSignalRouting(command[1], state);
        if (!signalExists) {System.out.println("Signal \"" + command[1] + "\" does not exist");}
    }


    private static void setPoint(String[] command, DiagramPanel diagram) {
        if (command.length != 3) {System.out.println("This command must have 2 arguments"); return;}

        int state = switch (command[2]) {
            case "n" -> Point.NORMAL;
            case "r" -> Point.REVERSE;
            case "both" -> Point.BOTH;
            case "neither" -> Point.NEITHER;
            default -> -1;
        };
        if (state == -1) {System.out.println("\"" + command[2] + "\" is not a valid point state"); return;}

        boolean pointExists = diagram.setPointState(command[1], state);
        if (!pointExists) {System.out.println("Point \"" + command[1] + "\" does not exist");}
    }


    private static void setTrackCircuit(String[] command, DiagramPanel diagram) {
        if (command.length != 3) {System.out.println("This command must have 2 arguments"); return;}

        int state = switch (command[2]) {
            case "occupied"-> TrackCircuit.OCCUPIED;
            case "unoccupied" -> TrackCircuit.UNOCCUPIED;
            default -> -1;
        };
        if (state == -1) {System.out.println("\"" + command[2] + "\" is not a valid TC state"); return;}

        boolean TCExists = diagram.setTrackCircuitState(command[1], state);
        if (!TCExists) {System.out.println("TrackCircuit \"" + command[1] + "\" does not exist");}
    }


    private static void setRoute(String[] command, DiagramPanel diagram) {
        if (command.length != 3) {System.out.println("This command must have 2 arguments"); return;}

        int state = switch (command[2]) {
            case "set" -> Route.SET;
            case "notset" -> Route.NOTSET;
            default -> -1;
        };
        if (state == -1) {System.out.println("\"" + command[2] + "\" is not a valid route state"); return;}

        boolean routeExists = diagram.setRouteState(command[1], state);
        if (!routeExists) {System.out.println("Route \"" + command[1] + "\" does not exist");}
    }


    /**
     * Adds labels to tracks, points, signals and berths based on the static booleans with the same names
     * eg: {@link #tracks}. This also marks any tracks with a track circuit as unoccupied
     * @param elements The element collection to label
     * @return A new element collection with {@link Text} labels added
     */
    private static ElementCollection populateDiagram(ElementCollection elements) {

        ArrayList<Text> texts = new ArrayList<>(elements.getTexts());

        if (tracks) {
            for (Track track : elements.getTracks().values()) {
                int x = (track.getAx()+ track.getBx())/2;
                int y = (track.getAy()+ track.getBy())/2;

                texts.add(new Text(
                        track.name,
                        x - (track.name.length()/2),
                        y,
                        new Color(255, 0, 0),
                        2,
                        Text.ARIAL_FONT
                ));
            }

        }
        if (points) {
            for (Point point: elements.getPoints().values()) {
                int x = (point.getNormalEnd() == 'A') ? point.getNormalTrack().getAx() : point.getNormalTrack().getBx();
                int y = (point.getNormalEnd() == 'A') ? point.getNormalTrack().getAy() : point.getNormalTrack().getBy();

                texts.add(new Text(
                        point.name,
                        x - (point.name.length()/2),
                        y-2,
                        new Color(0, 255, 0),
                        2,
                        Text.ARIAL_FONT
                ));
            }
        }
        if (signals) {
            for (Signal signal: elements.getSignals().values()) {
                int x = signal.getX();
                int y = signal.getY();

                texts.add(new Text(
                        signal.name,
                        x,
                        y-2,
                        new Color(0, 0, 255),
                        2,
                        Text.ARIAL_FONT
                ));
            }
        }
        if (berths) {
            for (Berth berth: elements.getBerths().values()) {
                berth.setDescriber(berth.name);
            }
        }

        for (TrackCircuit track: elements.getTrackCircuits().values()) {
            track.setState(TrackCircuit.UNOCCUPIED);
        }

        return new ElementCollection(
                new ArrayList<>(elements.getTracks().values()),
                new ArrayList<>(elements.getSignals().values()),
                new ArrayList<>(elements.getBerths().values()),
                texts,
                new ArrayList<>(elements.getRectangles().values()),
                new ArrayList<>(elements.getPoints().values()),
                new ArrayList<>(elements.getRoutes().values()),
                new ArrayList<>(elements.getTrackCircuits().values())
        );
    }
}
