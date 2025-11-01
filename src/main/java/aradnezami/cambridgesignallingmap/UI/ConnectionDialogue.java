package aradnezami.cambridgesignallingmap.UI;

import aradnezami.cambridgesignallingmap.NRFeed.Client.NRFeedException;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A utility class containing some static methods for displaying option dialogues using the
 * {@link JOptionPane} class, which prompts for which connection source is to be used. All
 * methods perform the exact same function but with slightly different prompts for different
 * contexts. The {@link #DIRECT_CHOSEN}, {@link #DATA_SERVER_CHOSEN} and {@link #QUIT_CHOSEN}
 * constants are the only values that can be returned from the methods and represent the
 * option chosen by the user
 */
public class ConnectionDialogue {

    private static final String[] options = {"Direct", "Data Server", "Quit"};

    /**
     * User closed the dialogue window without answering. Should be treated as a {@link #QUIT_CHOSEN}
     */
    private static final int DIALOGUE_CLOSED = JOptionPane.CLOSED_OPTION;
    /**
     * User has chosen to directly to connect the NR servers
     */
    private static final int DIRECT_CHOSEN = 0;
    /**
     * User has chosen to connect via the data server
     */
    private static final int DATA_SERVER_CHOSEN = 1;
    /**
     * User has chosen to close the program
     */
    private static final int QUIT_CHOSEN = 2;



    public static int displayInitialConnection(Component parent) {
        return JOptionPane.showOptionDialog(
                parent,
                "Chose a connection source",
                "Connect",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                "Direct"
        );
    }

    public static int displayChangeSource(Component parent) {
        return JOptionPane.showOptionDialog(
                parent,
                "Chose a new connection source",
                "Change Source",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                "Direct"
        );
    }

    public static int displayErrorReconnect(Component parent, NRFeedException e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();

        return JOptionPane.showOptionDialog(
                parent,
                "A connection error occurred:\n" + e.displayMessage + "\n Chose a new connection source",
                "Change Source",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                options,
                "Direct"
        );
    }

    public static int displayErrorReconnect(Component parent, Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();

        return JOptionPane.showOptionDialog(
                parent,
                "An error occurred:\n" + stackTrace + "\n Chose a new connection source",
                "Change Source",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                options,
                "Direct"
        );
    }
}
