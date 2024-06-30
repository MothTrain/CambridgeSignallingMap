package aradnezami.cambridgesignallingmap.NRFeedClient;

/**
 * The NRFeedClient interface serves as a superclass to classes which wrap clients
 * to the National Rail <a href="https://wiki.openraildata.com/index.php?title=TD">TD feed</a><br>
 * <br>
 * <h3>Death of NRFeedClients</h3>
 * An instance of an NRFeedClient dying can be caused by: A connection error with the NR servers or
 * a call to {@link #disconnect()}. In any case of a death, all resources associated with the connection
 * should be released. <br>
 * <br>
 * The time of death should be recorded and retrievable by {@link #disconnectedAt()}. This is so that
 * users can determine if reconnecting will allow them to retrieve their missed messages, if less than
 * 5 minutes have elapsed. This is only possible if a
 * <a href="https://wiki.openraildata.com/index.php?title=Durable_Subscription">durable connection</a>
 * is requested <br>
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
 *
 * @implNote According to the Network Rail's advice on
 * <a href="https://wiki.openraildata.com/index.php?title=Good_Practice">good practice</a>
 * , clients should "Process messages quickly". This <i>may</i> be achieved
 * by an internal thread which solely receives messages from the NR Feed and loads them onto a buffer,
 * which. {@link #pollNREvent()} queues on. <br>
 * This is especially important if a lack of calls to the implementor's pollNREvent()
 * would mean that messages would not get read from the NR feed. Causing messages to be missed or
 * for messages to be held up on NR's servers
 */
public interface NRFeedClient {
    /**
     * Returns the next <a href="https://wiki.openraildata.com/index.php?title=TD">TD</a>
     * event received from the NR Feed. These include both C (Berth) and S (Signalling)
     * class messages from the feed. The method must block until the next message is received<br>
     * If the underlying client fails (while or before the method was called), the method will return
     * null and the instance should be considered dead (see the {@link NRFeedClient class documentation}
     * on death).
     *
     * @return The next message or null if the client fails while polling
     * @throws IllegalStateException If the client is dead when called.
     * (see the {@link NRFeedClient class documentation})
     */
    public String pollNREvent() throws IllegalStateException;
    
    /**
     * Disconnects the NRFeedClient from the NR servers. The NRFeedClient is now dead and therefore,
     * subsequent calls to {@link #pollNREvent()} with throw {@link IllegalStateException}.
     * Calling this method when the instance is dead has no effect.
     */
    public void disconnect();
    
    /**
     * Returns the approximate time at which the NRFeedClient died (disconnected) in
     * millis since the unix epoch. This is approximate and system-dependant should
     * not be used for proper timestamping.
     * This is so that users can determine if reconnecting will allow them to
     * retrieve their missed messages, if less than 5 minutes have elapsed.
     * This is only possible if a durable connection is requested.
     * @return The approximate time in millis that the NRFeedClient died
     * @throws IllegalStateException If the instance is still alive
     *
     * @see System#currentTimeMillis()
     */
    public long disconnectedAt() throws IllegalStateException;
    
    /**
     * Checks the alive state of the NRFeedClient. If this returns false: all
     * subsequent calls to {@link #pollNREvent()} will throw {@link IllegalStateException}
     * @return True if the instance is alive and false if it is dead
     */
    public boolean isAlive();
}
