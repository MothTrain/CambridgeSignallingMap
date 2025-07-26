package aradnezami.cambridgesignallingmap.NRFeedClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PythonNRClientTest {
    private PythonNRClient pythonNRClient;

    @Mock
    private BufferedReader stdInMock;
    @Mock
    private BufferedReader stdErrMock;

    @Mock
    private Process processMock;

    private static final Logger logger = LogManager.getLogger(PythonNRClientTest.class);

    @BeforeEach
    void setupPythonNRClient() {
        MockitoAnnotations.openMocks(this);

        when(processMock.inputReader()).thenReturn(stdInMock);
        when(processMock.errorReader()).thenReturn(stdErrMock);

        pythonNRClient = new PythonNRClient(processMock);

        Configurator.setLevel(logger.getName(), org.apache.logging.log4j.Level.OFF);
    }



    @DisplayName("pollNREvent(): Normal Conditions")
    @Test
    void pollNREvent1() throws IOException {
        when(stdInMock.readLine()).thenReturn("A","B");

        assertEquals("A", pythonNRClient.pollNREvent());
        assertEquals("B", pythonNRClient.pollNREvent());

        verify(stdInMock, times(2)).readLine();
    }

    @DisplayName("pollNREvent(): IOException thrown")
    @Test()
    void pollNREvent2() {
        when(stdInMock).thenAnswer(invocationOnMock -> {throw new IOException();});

        assertThrows(NRFeedException.class, () -> pythonNRClient.pollNREvent());
        assertFalse(processMock.isAlive());

    }


    @DisplayName("disconnect()")
    @Test
    void disconnect() {
        pythonNRClient.disconnect();

        assertFalse(pythonNRClient.isAlive());
        assertThrows(NRFeedException.class, () -> {pythonNRClient.pollNREvent();});
    }



    @DisplayName("IsAlive(): returns true")
    @Test
    void isAlive() throws IOException {
        assertTrue(pythonNRClient.isAlive());
    }

}