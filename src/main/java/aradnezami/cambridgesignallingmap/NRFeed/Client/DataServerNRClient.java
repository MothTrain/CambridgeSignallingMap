package aradnezami.cambridgesignallingmap.NRFeed.Client;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

// TODO: update docs to reference the data server repo
/**
 * The {@link DataServerNRClient} wraps a {@link Socket} connection to a data server. This protects
 * the user from IOExceptions, replacing them with the standard {@link NRFeedException}. This instance
 * then becomes responsible for the socket and cleaning it up. Users should not attempt to interact
 * with the provided Socket thereafter.
 *
 * <h3>Death of PythonNRClient</h3>
 * An instance of an NRFeedClient dying can be caused by: A connection error with the NR servers or
 * a call to {@link #disconnect()}. In any case of a death, all resources associated with the connection
 * are released, and any subsequent calls to {@link #pollNREvent()} will throw {@link NRFeedException}.<br>
 * <br>
 */
public class DataServerNRClient implements NRFeedClient {
    private final Socket socket;
    private final BufferedReader in;


    /**
     * Creates an instance of DataServerNRClient using the given socket
     * @param socket Socket to be wrapped
     * @throws NRFeedException If the socket is closed or not connected, or if an IOException takes place.
     */
    public DataServerNRClient(Socket socket) throws NRFeedException {
        this.socket = socket;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socket.getOutputStream().write(1); // Forwarding connection type is 1
        } catch (IOException e) {
            throw new NRFeedException("Error whilst starting client.",
                    e,
                    "An internal error occurred while trying to connect. Ensure that your data server is online and that you have a connection");
        }
    }


    /**
     * Returns the next <a href="https://wiki.openraildata.com/index.php?title=TD">TD</a>
     * event received from the NR Feed. These include both C (Berth) and S (Signalling)
     * class messages from the feed. The method will block until the next message is received<br>
     *
     * <h3>Message Format</h3>
     * Messages are delineated by commas. S-Class messages come in the format {@code S,TIMESTAMP,ADDRESS,BYTE}
     * for example: {@code "S,12345678,A3,0D"}. Address and byte are in hexadecimal and are always 2 characters.
     * C-Class messages come in the format {@code C,TIMESTAMP,FROM_BERTH,TO_BERTH,DESCRIBER} for example:
     * {@code "C,12345678,0193,0195,1K76"}. A berth code is always a 4 character string. If either a to_berth
     * or from_berth is not provided, which will happen if the message is a berth cancel or interpose
     * respectively, the missing berth will be replaced with {@code "NONE"}
     *
     * @return The next message
     * @throws NRFeedException If a connection error occurs between the client and the data server
     */
    @Override
    public @NotNull String pollNREvent() throws NRFeedException {
        try {
            return in.readLine();
        } catch (IOException e) {
            throw new NRFeedException("Error whilst polling for event",
                    e,
                    "A connection error occurred whilst waiting for a message from the data feed. \nEnsure that you have a connection.");
        }
    }

    /**
     * Disconnects the DataServerNRClient. The DataServerNRClient is now dead and therefore,
     * subsequent calls to {@link #pollNREvent()} with throw {@link NRFeedException}.
     * Calling this method when the instance is dead has no effect.
     */
    @Override
    public void disconnect() {
        try {
            socket.close();
        } catch (IOException ignored) {}
    }

    /**
     * Checks if the client is dead or alive. If this returns false: all
     * subsequent calls to {@link #pollNREvent()} will throw {@link NRFeedException}
     *
     * @return True if the instance is alive and false if dead
     * @see Socket#isConnected()
     */
    @Override
    public boolean isAlive() {
        return socket.isConnected();
    }


    /**
     * Creates a socket that can be submitted to the constructor
     * @param host The hostname of the data server
     * @param port The port that the data server is listening on
     * @return A socket which can be submitted to the constructor
     * @throws NRFeedException If an IOException occurs while creating the socket
     * @see Socket#Socket(String, int) new Socket(host, port)
     */
    // TODO: replace with a factory pattern
    public static Socket getSocket(String host, int port) throws NRFeedException {
        try {
            return new Socket(host, port);
        } catch (IOException e) {
            throw new NRFeedException("Error whilst starting client.",
                    e,
                    "An internal error occurred while trying to connect. Ensure that your data server is online and that you have a connection");
        }
    }
}
