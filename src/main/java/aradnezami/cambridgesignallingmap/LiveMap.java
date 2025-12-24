package aradnezami.cambridgesignallingmap;

import aradnezami.cambridgesignallingmap.NRFeed.Client.DataServerNRClient;
import aradnezami.cambridgesignallingmap.NRFeed.Client.NRFeedClient;
import aradnezami.cambridgesignallingmap.NRFeed.Client.NRFeedException;
import aradnezami.cambridgesignallingmap.NRFeed.Client.PythonNRClient;
import aradnezami.cambridgesignallingmap.NRFeed.Event;
import aradnezami.cambridgesignallingmap.NRFeed.NRFeed;
import aradnezami.cambridgesignallingmap.NRFeed.SClassDecoder;
import aradnezami.cambridgesignallingmap.UI.ConnectionDialogue;
import aradnezami.cambridgesignallingmap.UI.DiagramElements.*;
import aradnezami.cambridgesignallingmap.UI.DiagramPanel;
import aradnezami.cambridgesignallingmap.UI.LiveDiagramMenuBar;
import aradnezami.cambridgesignallingmap.UI.LiveDiagramPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

/**
 * The "main" class of this repository. The creation of an instance of this class is sufficient to run
 * the full Cambridge Signalling Map application.
 */
public class LiveMap {
    private static final String DIAGRAM_PATH = "diagram.json";
    private static final String PYTHON_CLIENT_PATH = "src/main/java/aradnezami/cambridgesignallingmap/NRFeed/Client/PythonCommunications/main.py";
    private static final String DATA_SERVER_SECRETS_PATH = "DataServerSecrets.txt";
    private static final String DECODER_MAP_PATH = "SignallingEquipmentMap.csv";

    @NotNull
    private Thread nrFeedThread;

    private NRFeed feed;

    private DiagramPanel diagram;
    private LiveDiagramPanel diagramPanel;
    private final LiveDiagramMenuBar menuBar;
    private JFrame window;



    public static void main(String[] args) {
        LiveMap liveMap = new LiveMap();
    }


    public LiveMap() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException ignored) {}

        try {
            diagram = loadDiagramPanel();
            diagramPanel = new LiveDiagramPanel(diagram);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "The diagram couldn't be loaded, please ensure\n"+ DIAGRAM_PATH + " is present in the resources folder",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        menuBar = new LiveDiagramMenuBar();
        menuBar.setConnectionMenuEnabled(false);
        menuBar.addDisconnectAndCloseListener(e -> disconnectAndClose());
        menuBar.addChangeSourceListener(e -> changeSource());
        menuBar.addResetStateListener(e -> resetState());

        window = setupWindow(diagramPanel, menuBar);
        window.setVisible(true);


        try {
            int source = ConnectionDialogue.displayInitialConnection(window);
            feed = getFeed(source);
            menuBar.setConnectionMenuEnabled(true);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    "The decoder map couldn't be loaded, please ensure\n"+ DECODER_MAP_PATH + " is present in the resources folder",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        nrFeedThread = new Thread(nrFeedTask);
        nrFeedThread.start();
    }



    /**
     * A task runnable task that repeatedly polls on the feed and updates the diagram appropriately. To
     * interrupt this task, interrupt the thread running the task and then call on {@link NRFeed#disconnect()}
     */
    Runnable nrFeedTask = new Runnable() {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Event event = feed.nextEvent();
                    displayEvent(event);
                    diagramPanel.updateLastMsgClock();


                } catch (NRFeedException e) {
                    menuBar.setConnectionMenuEnabled(false);
                    feed.disconnect();

                    if (nrFeedThread.isInterrupted()) {
                        break;
                    } // Simply shutdown if interrupted

                    try {
                        int source = ConnectionDialogue.displayErrorReconnect(window, e);
                        feed = getFeed(source);

                        diagram.setElements(MapLoader.loadMap(LiveMap.DIAGRAM_PATH));
                        diagram.repaint();
                        menuBar.setConnectionMenuEnabled(true);
                    } catch (FileNotFoundException ex) {
                        JOptionPane.showMessageDialog(window,
                                "The decoder map couldn't be loaded, please ensure\n" + DECODER_MAP_PATH + " is present in the resources folder",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        disconnectAndClose();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(window,
                                "The diagram couldn't be loaded, please ensure\n" + DIAGRAM_PATH + " is present in the resources folder",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        disconnectAndClose();
                    }

                }


            }
        }
    };



    /*---------------------
     * USER INPUT CALLBACKS
     *--------------------- */


    /**
     * Interrupts the feed, closes and disposes of all UI resources and calls {@link System#exit(int)}
     * to terminate the program. This method is provided as a user input callback but is appropriate
     * for other uses
     */
    private void disconnectAndClose() {
        dispose();
        System.exit(0);
    }


    /**
     * Prompts the user to chose a source to change, interrupts the feed, replaces it and restarts
     * the feed thread. This method is provided as a user input callback
     */
    private void changeSource() {
        menuBar.setConnectionMenuEnabled(false);
        int source = ConnectionDialogue.displayChangeSource(window);

        interruptFeed();

        try {
            feed = getFeed(source);
            diagram.setElements(MapLoader.loadMap(LiveMap.DIAGRAM_PATH));
            diagram.repaint();
            menuBar.setConnectionMenuEnabled(true);

        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(window,
                    "The decoder map couldn't be loaded, please ensure\n" + DECODER_MAP_PATH + " is present in the resources folder",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            disconnectAndClose();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(window,
                    "The diagram couldn't be loaded, please ensure\n" + DIAGRAM_PATH + " is present in the resources folder",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            disconnectAndClose();
        }

        nrFeedThread = new Thread(nrFeedTask);
        nrFeedThread.start();
    }


    /**
     * Resets the {@link SClassDecoder} state and replaces the diagram with a blank diagram. This method is
     * provided as a user input callback
     */
    private void resetState() {
        try {
            feed.reset();
            diagram.setElements(MapLoader.loadMap(LiveMap.DIAGRAM_PATH));
            diagram.repaint();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(window,
                    "The diagram couldn't be loaded, please ensure\n" + DIAGRAM_PATH + " is present in the resources folder",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            disconnectAndClose();
        }
    }




    /**
     * Disconnects the feed and disposes of the UI elements. If this thread is interrupted or the feed thread takes too long to exit then
     * this method will not return.
     */
    private void dispose() {
        interruptFeed();

        window.setVisible(false);
        window.dispose();
    }


    /**
     * This method disconnects from the feed and interrupts the {@link #nrFeedThread}.
     * If this thread is interrupted or the feed thread takes too long to exit then
     * this method will not return.
     */
    private void interruptFeed() {
        nrFeedThread.interrupt();
        feed.disconnect();
        try {
            nrFeedThread.join(3000L);
            if (nrFeedThread.isAlive()) {throw new TimeoutException("NRFeedThread took too long to terminate");}

        } catch (InterruptedException | TimeoutException e) {
            JOptionPane.showMessageDialog(window,
                    "Could not change source because of an internal error",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            disconnectAndClose();
        }
    }


    /*---------------------
    * PROGRAM SETUP METHODS
    *---------------------- */

    /**
     * Returns the relevant {@link NRFeedClient} for the given source argument using the {@link ConnectionDialogue}
     * constants (eg: {@link ConnectionDialogue#DIRECT_CHOSEN}. If an error occurs the method will prompt the
     * user again itself. If the source is {@link ConnectionDialogue#QUIT_CHOSEN} or
     * {@link ConnectionDialogue#DIALOGUE_CLOSED}, this method will call {@link System#exit(int)} and will fail
     * to return
     * @param source The source to connect to
     * @return A client based on user choice
     */
    private @NotNull NRFeedClient getClient(int source) {
        return switch (source) {
            case ConnectionDialogue.DIALOGUE_CLOSED, ConnectionDialogue.QUIT_CHOSEN -> {
                System.exit(0);
                throw new RuntimeException("Will not happen");
            }

            case ConnectionDialogue.DIRECT_CHOSEN ->
                    new PythonNRClient(PythonNRClient.createPythonProcess(PYTHON_CLIENT_PATH));

            case ConnectionDialogue.DATA_SERVER_CHOSEN -> {
                try {
                    yield setupDataServerNRClient();
                } catch (IOException e) {
                    source = ConnectionDialogue.displayErrorReconnect(window, e);
                    yield getClient(source);
                } catch (NRFeedException e) {
                    source = ConnectionDialogue.displayErrorReconnect(window, e);
                    yield getClient(source);
                }
            }

            default -> throw new IllegalArgumentException("Should not happen: Unknown input received from dialogue");
        };
    }


    /**
     * Returns the relevant {@link NRFeed} for the given source argument using the {@link ConnectionDialogue}
     * constants (eg: {@link ConnectionDialogue#DIRECT_CHOSEN}. If an error occurs the method will prompt the
     * user again itself. Note that if the source is {@link ConnectionDialogue#QUIT_CHOSEN} or
     * {@link ConnectionDialogue#DIALOGUE_CLOSED}, this method will call {@link System#exit(int)} and will fail
     * to return
     * @return A connected NRFeed chosen by the user
     * @throws FileNotFoundException If the file of the feed's {@link SClassDecoder} could not be loaded
     */
    private NRFeed getFeed(int source) throws FileNotFoundException {
        NRFeedClient client = getClient(source);

        SClassDecoder decoder;
        decoder = new SClassDecoder(DECODER_MAP_PATH);
        return new NRFeed(client, decoder);
    }


    /**
     * Returns a {@link DataServerNRClient} connected to the address found in the file at {@link #DATA_SERVER_SECRETS_PATH}
     * @return A connected DataServerNRClient
     * @throws IOException If the data server secrets file could not be accessed or was incorrectly formatted
     */
    private DataServerNRClient setupDataServerNRClient() throws IOException {
        String[] secrets;

        ClassLoader classLoader = LiveMap.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(DATA_SERVER_SECRETS_PATH)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Could not load "+ DATA_SERVER_SECRETS_PATH +". Ensure that the file is present in the resources folder");
            }
            Scanner scanner = new Scanner(inputStream);

            secrets = scanner.nextLine().split(",");
            if (secrets.length != 2) {
                throw new IOException("Data server secrets had more than 2 comma delimited sections");
            }
        }

        String host = secrets[0];
        int port;
        try {
            port = Integer.parseInt(secrets[1]);
        } catch (NumberFormatException e) {
            throw new IOException("Could not data server secrets parse port number");
        }

        Socket socket = DataServerNRClient.getSocket(host, port);
        return new DataServerNRClient(socket);
    }


    /**
     * @return Loads diagram elements from {@link #DIAGRAM_PATH} and returns a diagram panel with
     * those elements
     * @throws IOException If the diagram file couldn't be accessed
     * @throws FontLoadingException If the fonts used by the diagram couldn't be accessed
     * @throws DiagramFormatException If the diagram was accessed but was incorrectly formated
     */
    private DiagramPanel loadDiagramPanel() throws IOException, FontLoadingException, DiagramFormatException {
        ElementCollection elements = MapLoader.loadMap(LiveMap.DIAGRAM_PATH);
        return new DiagramPanel(elements);
    }


    /**
     * Returns a {@link JFrame} with a default dimension, title and adds the given diagram and
     * menubar to the frame
     * @param diagram The diagram to be displayed
     * @param menuBar The menubar to be displayed
     * @return A set-up jframe
     */
    private JFrame setupWindow(LiveDiagramPanel diagram, JMenuBar menuBar) {
        window = new JFrame("Cambridge Signalling Map");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        window.setJMenuBar(menuBar);
        window.add(diagram);
        window.setSize(1000,700);

        return window;
    }



    /**
     * Enacts the provided S-Class event event on the {@link #diagram}
     * @param event The event to display
     * @throws IllegalArgumentException If the event had an invalid {@link Event#S_State}, {@link Event#S_Type}
     * or {@link Event#C_Describer}
     */
    private void displayEvent(Event event) {
        if (event.type == 'C') {
            if (event.C_FromBerth != null) {diagram.setBerth(event.C_FromBerth, "");}
            if (event.C_ToBerth != null) {diagram.setBerth(event.C_ToBerth, event.C_Describer);}
        } else if (event.type == 'S') {
            displaySClassEvent(event);
        }

        diagram.repaint();
    }


    /**
     * Enacts the provided S-Class event event on the {@link #diagram}
     * @param event The event to display
     * @throws IllegalArgumentException If the event had an invalid {@link Event#S_State} or {@link Event#S_Type}
     */
    @SuppressWarnings("DataFlowIssue")
    private void displaySClassEvent(Event event) {
        switch (event.S_Type) {
            case Signal.ASPECT_TYPE: diagram.setSignalAspect(event.S_Id, event.S_State); break;
            case Signal.ROUTED_TYPE: diagram.setSignalRouting(event.S_Id, event.S_State); break;
            case Point.TYPE: diagram.setPointState(event.S_Id, event.S_State); break;
            case TrackCircuit.TYPE: diagram.setTrackCircuitState(event.S_Id, event.S_State); break;

            case Route.MAIN_TYPE,
                 Route.SHUNT_TYPE,
                 Route.CALL_ON_TYPE:
                 diagram.setRouteState(event.S_Id, event.S_State); break;

            default:
                throw new IllegalArgumentException(event.S_Type + " is not a valid S-Class event to display");
        }
    }

}
