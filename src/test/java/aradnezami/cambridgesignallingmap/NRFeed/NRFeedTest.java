package aradnezami.cambridgesignallingmap.NRFeed;

import aradnezami.cambridgesignallingmap.UI.DiagramElements.Point;
import aradnezami.cambridgesignallingmap.UI.DiagramElements.Signal;
import aradnezami.cambridgesignallingmap.NRFeed.Client.NRFeedClient;
import aradnezami.cambridgesignallingmap.NRFeed.Client.NRFeedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class NRFeedTest {
    private NRFeed feed;

    @Mock
    private NRFeedClient client;
    @Mock
    private SClassDecoder decoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        feed = new NRFeed(client, decoder);
    }


    @Test
    @DisplayName("nextEvent(): one SClass Event decoded")
    void nextEvent1() {
        Event expected1 = new Event(-1L, Signal.ASPECT_TYPE, Signal.MAIN_OFF, "123");
        Event expected2 = new Event(-1L, Signal.ASPECT_TYPE, Signal.MAIN_OFF, "456");
        when(client.pollNREvent()).thenReturn("s,1,2,3,4,5"); // Value doesn't matter
        when(decoder.SClassChange(anyLong(), anyInt(), anyInt()))
                .thenReturn(new Event[]{expected1})
                .thenReturn(new Event[]{expected2}); // Second value just ensures a value isn't returned again

        assertEquals(expected1, feed.nextEvent());
        assertEquals(expected2, feed.nextEvent());
        verify(client, times(2)).pollNREvent();
        verify(decoder, times(2)).SClassChange(anyLong(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("nextEvent(): multiple SClass events decoded")
    void nextEvent2() {

        Event expected1 = new Event(-1L, Signal.ASPECT_TYPE, Signal.MAIN_OFF, "123");
        Event expected2 = new Event(-1L, Signal.ASPECT_TYPE, Signal.MAIN_OFF, "456");
        Event expected3 = new Event(-1L, Point.TYPE, Point.REVERSE, "4567");

        when(client.pollNREvent()).thenReturn("s,1,2,3,4,5"); // Value doesn't matter
        when(decoder.SClassChange(anyLong(), anyInt(), anyInt()))
                .thenReturn(new Event[]{expected1, expected2})
                .thenReturn(new Event[]{expected3});


        assertEquals(expected1, feed.nextEvent());
        assertEquals(expected2, feed.nextEvent());
        assertEquals(expected3, feed.nextEvent());
        verify(client, times(2)).pollNREvent();
        verify(decoder, times(2)).SClassChange(anyLong(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("nextEvent(): no SClass Event decoded")
    void nextEvent3() {
        Event expected = new Event(-1L, Signal.ASPECT_TYPE, Signal.MAIN_OFF, "123");
        when(client.pollNREvent()).thenReturn("s,1,2,3,4,5"); // Value doesn't matter
        when(decoder.SClassChange(anyLong(), anyInt(), anyInt()))
                .thenReturn(new Event[]{}) // No event decoded
                .thenReturn(new Event[]{expected}); // Second value just ensures a value isn't returned again

        assertEquals(expected, feed.nextEvent());
        verify(client, times(2)).pollNREvent();
        verify(decoder, times(2)).SClassChange(anyLong(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("nextEvent(): C-Class Event")
    void nextEvent4() {
        Event expected = new Event(-1L, "0123", "0456", "1K67");
        when(client.pollNREvent()).thenReturn("C,-1,0123,0456,1K67");

        assertEquals(expected, feed.nextEvent());
        verifyNoInteractions(decoder);
    }

    @Test
    @DisplayName("nextEvent(): NRFeedException")
    void nextEvent5() {
        when(client.pollNREvent()).thenThrow(new NRFeedException("msg", "Display msg"));

        assertThrows(NRFeedException.class, () -> feed.nextEvent());
    }


    @Test
    void disconnect() {
        feed.disconnect();

        verify(client, times(1)).disconnect();
        // We dont need to check that nextEvent() throws an exception. This behaviour is managed by the NRFeedClient.
        // Any further testing of isalive or nextEvent, would simply be testing the NRFeedClient
    }

    @Test
    void isAlive() {
        when(client.isAlive())
                .thenReturn(true)
                .thenReturn(false);

        assertTrue(feed.isAlive());
        assertFalse(feed.isAlive());

        verify(client, times(2)).isAlive();
    }


    @Test
    void reset() {
        feed.reset();

        verify(decoder, times(1)).reset();
    }
}