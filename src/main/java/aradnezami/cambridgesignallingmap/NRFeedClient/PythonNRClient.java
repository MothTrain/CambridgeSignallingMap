package aradnezami.cambridgesignallingmap.NRFeedClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;


/**
 * The PythonNRClient wraps a python process that receives data from the Network Rail
 * <a href="https://wiki.openraildata.com/index.php?title=TD">TD feed</a><br>. This implementation does
 * not request a durable subscription, so {@link #disconnectedAt()} is not useful. It is still ,however,
 * implemented
 * <br>
 * <h3>Death of PythonNRClients</h3>
 * An instance of an PythonNRClient dying can be caused by: A connection error with the NR servers or
 * a call to {@link #disconnect()}. In any case of a death, all resources associated with the connection
 * should be released. <br>
 * <br>
 * If {@link #disconnect()} is called, a call to {@link #isAlive()} returns false, or a previous poll
 * returned null: all subsequent polls will throw {@link IllegalStateException} as the client is dead.
 * However, if the client died before or during a poll and none of
 * the above conditions were satisfied beforehand, then null will be returned. All subsequent polls
 * will throw {@link IllegalStateException} <br>
 * The convoluted conditions of the client death means that if {@link #disconnect()} is called,
 * a call to {@link #isAlive()} returns false, or a previous poll returned null, are the only
 * signals a user needs to know that the client is dead. <b>A client that handles the NRFeedClient as
 * specified should never encounter an illegalStateException</b>
 * <br>
 * Note that {@link #checkError()} is included when reference is made to "polling"
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
     * Creates a connected PythonNRClient. No further setup is required to poll the client
     * @throws IOException If an IOException occurs while starting the python process
     * ({@link ProcessBuilder#start()})
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
     * If the underlying client fails (while or before the method was called), the method will return
     * null and the instance should be considered dead (see the {@link NRFeedClient interface documentation}
     * on death).<br>
     * <h3>Message Format</h3>
     * Messages are delineated by commas. S-Class messages come in the format {@code S,ADDRESS,BYTE}
     * for example: {@code "S,A3,0D"}. Address and byte are in hexadecimal and are always 2 characters.
     * C-Class messages come in the format {@code C,FROM_BERTH,TO_BERTH,DESCRIBER} for example:
     * {@code "C,0193,0195,1K76"}. A berth code is always a 4 character string. If either a to_berth
     * or from_berth is not provided, which will happen if the message is a berth cancel or interpose
     * respectively, the missing berth will be replaced with {@code "NONE"}
     *
     * @return The next message or null if the client fails while polling
     * @throws IllegalStateException If the client is dead when called.
     *                               (see the {@link NRFeedClient interface documentation})
     */
    @Override
    public String pollNREvent() throws IllegalStateException {
        if (!isAlive) {
            throw new IllegalStateException("The PythonNRClient has died");
        }
        
        try {
            return stdIn.nextLine();
        } catch (NoSuchElementException e) {
            disconnect();
            return null;
        }
    }
    
    /**
     * Returns the full contents of the python client's error stream. If no content is available
     * the method will not block and will immediately return an empty string. If the client has
     * failed then the method will return null and the instance should be considered dead
     * @return The contents of the python client's error stream
     * @throws IllegalStateException If the PythonNRClient is dead
     */
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
            return null;
        }
        
        return sb.toString();
    }
    
    /**
     * Disconnects the PythonNRClient from the NR servers. The PythonNRClient is now dead and therefore,
     * subsequent calls to {@link #pollNREvent()} with throw {@link IllegalStateException}.
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
     * , which this implementation does not make.
     *
     * @return The approximate time in millis that the PythonNRClient died
     * @throws IllegalStateException If the instance is still alive
     * @see System#currentTimeMillis()
     */
    @Override
    public long disconnectedAt() throws IllegalStateException {
        if (isAlive) {throw new IllegalStateException("Cannot get disconnect time if the instance is alive");}
        
        return disconnectTime;
    }
    
    /**
     * Checks the alive state of the PythonNRClient. If this returns false: all
     * subsequent calls to {@link #pollNREvent()} will throw {@link IllegalStateException}
     *
     * @return True if the instance is alive and false if it is dead
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
