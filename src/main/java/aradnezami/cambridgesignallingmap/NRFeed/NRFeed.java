package aradnezami.cambridgesignallingmap.NRFeed;

import aradnezami.cambridgesignallingmap.NRFeed.Client.NRFeedClient;
import aradnezami.cambridgesignallingmap.NRFeed.Client.NRFeedException;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * The NRFeedClient is used by users of the package to access the Network Rail data feeds and returns
 * {@link Event}s in a decoded form.
 */
public class NRFeed {
    private final SClassDecoder decoder;
    private final NRFeedClient client;

    private final ArrayList<Event> bufferedEvents = new ArrayList<>();


    public final int TYPE = 0;
    public final int TIMESTAMP = 1;

    public final int S_ADDRESS = 2;
    public final int S_BYTE = 3;

    public final int C_FROMBERTH = 2;
    public final int C_TOBERTH = 3;
    public final int C_DESCRIBER = 4;


    /**
     * Creates an instance of NRFeed using the provided NRFeedClient as the data feed and
     * the decoder for understanding S-Class messages. The instance becomes responsible for
     * handling the client, so the user should not interact with the client hereafter
     * @param client NRFeedClient to be used
     * @param decoder SClassDecoder to be used
     */
    public NRFeed(NRFeedClient client, SClassDecoder decoder) {
        this.decoder = decoder;
        this.client = client;
    }


    /**
     * Polls for the next {@link Event} from the feed. If no event is available, the method
     * blocks until one is received. If an S-Class event constitutes several changes, the
     * method will return the events sequentially in the order of the bit number in the mappings.
     * @return The most recently received event from the feed
     * @throws NRFeedException If a connection error occurs
     */
    public Event nextEvent() throws NRFeedException {
        if (!bufferedEvents.isEmpty()) {
            Event event = bufferedEvents.get(0);
            bufferedEvents.remove(0);
            return event;
        }

        String[] eventStr = client.pollNREvent().split(",");
        long timestamp = Long.parseLong(eventStr[TIMESTAMP]);

        if (eventStr[TYPE].equals("C")) {
            return new Event(timestamp, eventStr[C_FROMBERTH], eventStr[C_TOBERTH], eventStr[C_DESCRIBER]);
        }

        int address = Integer.parseInt(eventStr[S_ADDRESS], 16);
        int data = Integer.parseInt(eventStr[S_BYTE], 16);
        Event[] events = decoder.SClassChange(timestamp, address, data);

        if (events.length == 0) {
            return nextEvent();
        } else if (events.length == 1) {
            return events[0];
        } else {
            bufferedEvents.addAll(Arrays.asList(events).subList(1, events.length)); // buffer all *except 1st event*
            return events[0];
        }
    }

    /**
     *  Resets all SClass state in the instance. The instance will behave as though
     *  it has just been constructed with no knowledge of any S-Class state.
     */
    public void reset() {
        decoder.reset();
    }


    /**
     * Checks if this instance is alive. If false is returned, all subsequent calls to {@link #nextEvent()}
     * will throw {@link NRFeedException}
     * @return True if instance is alive, false otherwise
     */
    public boolean isAlive() {
        return client.isAlive();
    }

    /**
     * Disconnects the instance from the feed
     */
    public void disconnect() {
        client.disconnect();
    }
}
