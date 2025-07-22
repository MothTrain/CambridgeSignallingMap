package aradnezami.cambridgesignallingmap.NRFeedClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;


/**
 * The PythonNRClient wraps a python process that receives data from the Network Rail
 * <a href="https://wiki.openraildata.com/index.php?title=TD">TD feed</a>.
 *
 * <h3>Death of PythonNRClient</h3>
 * An instance of an NRFeedClient dying can be caused by: A connection error with the NR servers or
 * a call to {@link #disconnect()}. In any case of a death, all resources associated with the connection
 * are released, and any subsequent calls to {@link #pollNREvent()} will throw {@link NRFeedException}.<br>
 * <br>
 */
public class PythonNRClient implements NRFeedClient {
    private final Logger logger = LogManager.getLogger();
    private final static String pythonScriptPath = "src/main/java/aradnezami/cambridgesignallingmap/NRFeedClient/PythonCommunications/main.py";
    
    private boolean isAlive;
    private long disconnectTime;
    private final Scanner stdIn;
    private final InputStream errIn;
    private Process pythonClientProcess;
    
    private final Thread exitHook = new Thread(() -> pythonClientProcess.destroy());
    private Thread processWaiter = null;
    
    
    /**
     * Creates a connected PythonNRClient, which is ready to be polled
     */
    public PythonNRClient() throws IOException {
        pythonClientProcess = createPythonClientProcess();
        stdIn = createScanner(pythonClientProcess.getInputStream());
        errIn = pythonClientProcess.getErrorStream();
        
        Runtime.getRuntime().addShutdownHook(exitHook);
        processWaiter = createProcessWaiterThread();
        processWaiter.start();
        
        isAlive = true;
    }
    
    /**
     * Returns the next <a href="https://wiki.openraildata.com/index.php?title=TD">TD</a>
     * event received from the NR Feed. These include both C (Berth) and S (Signalling)
     * class messages from the feed. The method must block until the next message is received<br>
     *
     * <h3>Message Format</h3>
     * Messages are delineated by commas. S-Class messages come in the format {@code S,ADDRESS,BYTE}
     * for example: {@code "S,A3,0D"}. Address and byte are in hexadecimal and are always 2 characters.
     * C-Class messages come in the format {@code C,FROM_BERTH,TO_BERTH,DESCRIBER} for example:
     * {@code "C,0193,0195,1K76"}. A berth code is always a 4 character string. If either a to_berth
     * or from_berth is not provided, which will happen if the message is a berth cancel or interpose
     * respectively, the missing berth will be replaced with {@code "NONE"}
     *
     * @return The next message or null if the client fails while polling
     * @throws NRFeedException If a connection error occurs between the client and python script
     * or if the client was already dead when called
     */
    @Override
    public @NotNull String pollNREvent() {
        if (!isAlive) {
            throw new NRFeedException("The PythonNRClient is already dead. Method should not be" +
                    " called when client is dead",
                    "An internal error occurred. This should not happen. Please report this" +
                    " as a bug, along with the contents of \"More info\"");
        }

        try {
            return stdIn.nextLine();
        } catch (NoSuchElementException e) {
            disconnect();
            throw new NRFeedException("Error whilst polling for event",
                    e,
                    "A connection error occurred whilst waiting for a message from the data feed. \n" +
                            "Ensure that you have an internet connection and that your authentication" +
                            " details are correct.");
        }
    }
    
    /**
     * Returns the full contents of the python client's error stream. If no content is available
     * the method will not block and will immediately return an empty string.
     *
     * @return The contents of the python client's error stream
     * @throws NRFeedException If a connection error occurs between the client and python script
     * or if the client was already dead when called
     */
    @NotNull
    public String checkError() throws IllegalStateException {
        if (!isAlive) {
            throw new IllegalStateException("The PythonNRClient has died");
        }
        
        StringBuilder sb = new StringBuilder();
        try {
            while (errIn.available() > 0) {
                sb.append((char) errIn.read());
            }
        } catch (IOException e) {
            disconnect();
            throw new NRFeedException("Error whilst polling on the python client's error stream",
                    e,
                    "A connection error occurred whilst waiting for error messages from the data feed. \n" +
                            "Ensure that you have an internet connection and that your authentication" +
                            " details are correct.");
        }
        
        return sb.toString();
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
        disconnectTime = System.currentTimeMillis();
        
        Runtime.getRuntime().removeShutdownHook(exitHook);
        
        processWaiter.interrupt();
        
        if (processWaiter.equals(Thread.currentThread())) {
            return; // Mustn't wait for processWaiter to join() if this is called by the process waiter2
        }
        try {
            processWaiter.join();
        } catch (InterruptedException e) {
            logger.debug("disconnect() interrupted while waiting for process waiter to terminate",e);
        }
    }

    /**
     * Returns the approximate time at which the PythonNRClient died (disconnected) in
     * millis since the unix epoch. This is approximate and system-dependant should
     * not be used for proper timestamping.
     * This is method is implemented but not useful, as it is intended for users to
     * determine if they will retrieve their messages if they reconnect within 5 minutes.
     * This is only possible with a <a href="https://wiki.openraildata.com/index.php?title=Durable_Subscription">
     *
     * @return The approximate time in millis that the PythonNRClient died
     * @throws IllegalStateException If the instance is still alive
     * @see System#currentTimeMillis()
     */
    public long disconnectedAt() throws IllegalStateException {
        if (isAlive) {throw new IllegalStateException("Cannot get disconnect time if the instance is alive");}
        
        return disconnectTime;
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
     * Creates a process, wrapping the python client. The standard inputstream of the process
     * provides NR messages in the format that is returned by {@link #pollNREvent()}
     * The method uses the {@code pythonScriptPath} field as the path of the python file
     * @return The python process
     * @throws IOException If an IO error occurs when the process is started
     */
    protected Process createPythonClientProcess() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("Python", pythonScriptPath);
        
        return processBuilder.start();
    }
    
    /**
     * Wraps an InputStream in a Scanner. This is provided for ease of unit testing:
     * it is overridden in the test to return a mocked Scanner.
     * @param inputStream InputStream to wrap
     * @return The InputStream wrapped in a Scanner
     */
    protected Scanner createScanner(InputStream inputStream) {
        return new Scanner(inputStream);
    }
    
    protected Thread createProcessWaiterThread() {
        return new Thread(() -> {
            try {
                pythonClientProcess.waitFor();
            } catch (InterruptedException e) {
                logger.debug("Python process waiter interrupted", e);
                isAlive = false;
                return;
            }
            logger.debug("Python process ended");
            disconnect();
            
            isAlive = false;
        });
    }
    
}
