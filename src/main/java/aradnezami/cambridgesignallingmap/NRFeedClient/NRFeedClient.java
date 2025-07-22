package aradnezami.cambridgesignallingmap.NRFeedClient;

import org.jetbrains.annotations.NotNull;

/**
 * The NRFeedClient (Network Rail Feed Client) interface serves as a superclass to classes
 * which wrap clients to the National Rail
 *  <a href="https://wiki.openraildata.com/index.php?title=TD">TD feed</a>,
 * abstracting the underlying form of connection to the NR feed.
 * <br>
 * <h3>Death of NRFeedClients</h3>
 * An instance of an NRFeedClient dying can be caused by: A connection error with the NR servers or
 * a call to {@link #disconnect()}. In any case of a death, all resources associated with the connection
 * will be released, and any subsequent calls to {@link #pollNREvent()} will throw{@link NRFeedException}.<br>
 * <br>

 *
 * @implSpec   According to the Network Rail's advice on
 * <a href="https://wiki.openraildata.com/index.php?title=Good_Practice">good practice</a>,
 * clients should "Process messages quickly". Ensure that a lack of calls to
 * {@link #pollNREvent()}, would not result in messages not being processed, so that messages
 * from the feed are not missed, or (in the case of a durable connection) messages are
 * acknowledged.
 */
public interface NRFeedClient {
    /**
     * Returns the next <a href="https://wiki.openraildata.com/index.php?title=TD">TD</a>
     * event received from the NR Feed. These include both C (Berth) and S (Signalling)
     * class messages from the feed. If not message is available, the method blocks until
     * the next message is received<br>
     *
     * Messages are delineated by commas. S-Class messages come in the format {@code S,ADDRESS,BYTE}
     * for example: {@code "S,A3,0D"}. Address and byte are in hexadecimal and are always 2 characters.
     * C-Class messages come in the format {@code C,FROM_BERTH,TO_BERTH,DESCRIBER} for example:
     * {@code "C,0193,0195,1K76"}. A berth code is always a 4 character string. If either a to_berth
     * or from_berth is not provided, which will happen if the message is a berth cancel or interpose
     * respectively, the missing berth will be replaced with {@code "NONE"}
     *
     * @return The next message
     * @throws NRFeedException If a connection error occurs or if the client was already dead
     */
    @NotNull String pollNREvent() throws NRFeedException;
    
    /**
     * Disconnects the NRFeedClient. The NRFeedClient is now dead and therefore,
     * subsequent calls to {@link #pollNREvent()} will throw {@link NRFeedException}.
     * Calling this method when the instance is dead has no effect.
     */
    void disconnect();
    
    /**
     * Checks the alive state of the NRFeedClient. If this returns false: all
     * subsequent calls to {@link #pollNREvent()} will throw {@link NRFeedException}
     * @return True if the instance is alive and false if it is dead
     */
    boolean isAlive();
}
