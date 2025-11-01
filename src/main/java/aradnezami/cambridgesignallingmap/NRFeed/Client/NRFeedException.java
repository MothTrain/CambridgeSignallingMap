package aradnezami.cambridgesignallingmap.NRFeed.Client;

/**
 * NRFeedException represents exceptions thrown, when a <b>fatal</b> connection error occurs
 * with the NRFeed. Since this represents fatal errors, users of this package should not find
 * that this exception is thrown multiple times in the same connection, as when this is thrown
 * the feed should be disposed of
 * <p>
 * NRFeedExceptions are designed to be displayed to users. As such, the NRFeedException
 * is designed to facilitate this by including a {@link #displayMessage}, which can be shown in a
 * dialogue box to the user. This was differentiated from the {@link RuntimeException#detailMessage detailMessage},
 * which could contain more technical information, useful to developers, whilst the display message
 * can contain user-friendly information about the error and possibly suggestions for fixes.
 * It is recommended that users of this exception provide the underlying cause exception.
 */
public class NRFeedException extends RuntimeException {
    public final String displayMessage;


    public NRFeedException(String message, String displayMessage) {
        super(message);
        this.displayMessage = displayMessage;
    }

    public NRFeedException(String message, Throwable cause, String displayMessage) {
        super(message, cause);
        this.displayMessage = displayMessage;
    }
}
