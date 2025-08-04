package aradnezami.cambridgesignallingmap.NRFeed.Client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DataServerNRClientTest {
    private DataServerNRClient client;

    @Mock
    private InputStream inputStream;
    @Mock
    private Socket socket;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

    }

    @DisplayName("pollNREvent(): Normal conditions")
    @Test
    void pollNREvent1() {
        String expected = "S,1234,A4,5D";
        inputStream = new ByteArrayInputStream((expected+"\n").getBytes());

        try {
            when(socket.getInputStream()).thenReturn(inputStream);
        } catch (IOException ignored) {}
        client = new DataServerNRClient(socket);

        assertEquals(expected, client.pollNREvent());
    }

    @DisplayName("pollNREvent(): IOException")
    @Test
    void pollNREvent2() {
        try {
            when(inputStream.read()).thenThrow(new IOException());
            when(inputStream.read(any())).thenThrow(new IOException());
            when(inputStream.read(any(), anyInt(), anyInt())).thenThrow(new IOException());
            when(inputStream.readAllBytes()).thenThrow(new IOException());
            when(inputStream.readNBytes(anyInt())).thenThrow(new IOException());
            when(inputStream.readNBytes(any(), anyInt(), anyInt())).thenThrow(new IOException());

            when(socket.getInputStream()).thenReturn(inputStream);
        } catch (IOException ignored) {}
        client = new DataServerNRClient(socket);

        assertThrows(NRFeedException.class, () -> client.pollNREvent());
    }

    @Test
    void disconnect() {
        try {
            when(socket.getInputStream()).thenReturn(inputStream);
        } catch (IOException ignored) {}
        client = new DataServerNRClient(socket);

        client.disconnect();

        assertFalse(client.isAlive());
        assertThrows(NRFeedException.class, () -> client.pollNREvent());
    }

    @Test
    void isAlive() {
        when(socket.isConnected()).thenReturn(true);
        try {
            when(socket.getInputStream()).thenReturn(inputStream);
        } catch (IOException ignored) {}

        client = new DataServerNRClient(socket);

        assertTrue(client.isAlive()); // client.isAlive() == False is tested in disconnect
    }

}