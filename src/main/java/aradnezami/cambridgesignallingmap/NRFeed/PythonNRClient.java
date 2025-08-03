package aradnezami.cambridgesignallingmap.NRFeed;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * The PythonNRClient wraps a provided {@link Process} that provides data from the Network Rail
 * <a href="https://wiki.openraildata.com/index.php?title=TD">TD feed</a>, through its standard
 * output stream and then allows the data to be polled on conveniently. The PythonNRClient becomes
 * responsible for handling and cleaning up the process. Users should not attempt to interact
 * with the provided process thereafter.
 *
 * <h3>Data Format</h3>
 * Messages sent through the standard output stream must be provided in the following format.
 * Format of messages may not be checked. As such, processes that provide incorrectly formatted
 * data, may cause the PythonNRClient to output incorrectly formatted data (i.e: Garbage in:
 * Garbage out) <br>
 * Messages are terminated by a new line (any new line format accepted by {@link Reader}
 * is acceptable). Messages should delineated by commas. S-Class messages are in the format
 * {@code S,TIMESTAMP,ADDRESS,BYTE} for example: {@code "S,12345678,A3,0D"}. Address and byte are in hexadecimal
 * and are always 2 characters. C-Class messages come in the format {@code C,TIMESTAMP,FROM_BERTH,TO_BERTH,DESCRIBER}
 * for example: {@code "C,12345678,0193,0195,1K76"}. A berth code is always a 4 character string. If either
 * a to_berth or from_berth is not provided, which will happen if the message is a berth cancel
 * or interpose respectively, the missing berth should be replaced with {@code "NONE"}
 *
 * <h3>Death of PythonNRClient</h3>
 * An instance of an NRFeedClient dying can be caused by: A connection error with the NR servers or
 * a call to {@link #disconnect()}. In any case of a death, all resources associated with the connection
 * are released, and any subsequent calls to {@link #pollNREvent()} will throw {@link NRFeedException}.<br>
 * <br>
 */
public class PythonNRClient implements NRFeedClient {
    private boolean isAlive;
    private final BufferedReader stdIn;
    private final Process pythonClientProcess;
    
    private final Thread exitHook;
    
    /**
     * Creates a connected PythonNRClient from the provided {@link Process}
     *
     * @param pythonClientProcess The process to be wrapped by PythonNRClient
     */
    public PythonNRClient(Process pythonClientProcess) {
        this.pythonClientProcess = pythonClientProcess;
        stdIn = pythonClientProcess.inputReader();

        this.exitHook = new Thread(pythonClientProcess::destroy);
        Runtime.getRuntime().addShutdownHook(exitHook);
        
        isAlive = true;
    }

    
    /**
     * Returns the next <a href="https://wiki.openraildata.com/index.php?title=TD">TD</a>
     * event received from the NR Feed. These include both C (Berth) and S (Signalling)
     * class messages from the feed. The method must block until the next message is received<br>
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
     * @throws NRFeedException If a connection error occurs between the client and python script
     * or if the client was already dead when called
     */
    @Override
    public @NotNull String pollNREvent() {
        if (!isAlive) {
            throw new NRFeedException("The PythonNRClient is already dead. Method should not be called when client is dead",
                    "An internal error occurred, while waiting for a message from the feed. This should not happen. \nPlease report this as a bug, along with the contents of \"More info\"");
        }

        try {
            String msg = stdIn.readLine();
            if (msg == null) {
                isAlive = false;
                throw new NRFeedException("Null returned whilst polling for event",
                    "A connection error occurred whilst waiting for a message from the data feed. \nEnsure that you have an internet connection and -secondarily- that your authentication details are correct.");
            }

            return msg;
        } catch (IOException e) {
            disconnect();
            throw new NRFeedException("Error whilst polling for event",
                    e,
                    "A connection error occurred whilst waiting for a message from the data feed. \nEnsure that you have an internet connection and -secondarily- that your authentication details are correct.");
        }
    }

    
    /**
     * Disconnects the PythonNRClient. The PythonNRClient is now dead and therefore,
     * subsequent calls to {@link #pollNREvent()} with throw {@link NRFeedException}.
     * Calling this method when the instance is dead has no effect.
     */
    @Override
    public void disconnect() {
        if (!isAlive) {return;}
        isAlive = false;

        Runtime.getRuntime().removeShutdownHook(exitHook);

        try {
            pythonClientProcess.destroy();
            boolean hasTerminated = pythonClientProcess.waitFor(1L, TimeUnit.SECONDS);
            if (!hasTerminated) {pythonClientProcess.destroyForcibly();}

        } catch (InterruptedException e) {
            throw new NRFeedException("Calling thread was interrupted whilst waiting for python process to terminate",
                    e,
                    "An internal error occurred, while disconnecting from the feed. This should not happen. Please report this as a bug, along with the contents of \"More info\"");
        }
    }

    /**
     * Checks if the client is dead or alive. If this returns false: all
     * subsequent calls to {@link #pollNREvent()} will throw {@link NRFeedException}
     *
     * @return True if the instance is alive and false if dead
     */
    @Override
    public boolean isAlive() {
        return isAlive;
    }


    /**
     * Creates a python process that can be submitted to the constructor using the path
     * provided. The user should then <b>promptly</b> submit this to a constructor
     * @param path The path to the python file
     * @return A an executing process of the file given
     */
    public static Process createPythonProcess(String path) {
        ProcessBuilder pb = new ProcessBuilder("python", path);

        try {
            return pb.start();
        } catch (IOException e) {
            throw new NRFeedException("Error whilst starting client. Check the path is correct",
                    e,
                    "An internal error occurred while trying to connect. This should not happen. Please report this as a bug, along with the contents of \"More info\"");
        }
    }
}
