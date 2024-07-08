package aradnezami.cambridgesignallingmap.NRFeedClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PythonNRClientTest {
    @Spy
    private PythonNRClient pythonNRClient;
    
    @Mock
    private InputStream stdInMock;
    @Mock
    private Scanner scannerMock;
    @Mock
    private InputStream errInMock;
    
    @Mock
    private Process processMock;
    @Mock
    private Thread processWaiterMock;
    
    private static final Logger logger = LogManager.getLogger(PythonNRClient.class);
    
    @BeforeEach
    void setupPythonNRClient() throws IOException {
        MockitoAnnotations.openMocks(this);
        
        when(processMock.getErrorStream()).thenReturn(errInMock);
        when(processMock.getInputStream()).thenReturn(stdInMock);
        
        Configurator.setLevel(logger.getName(), org.apache.logging.log4j.Level.OFF);
    }
    
    
    
    @DisplayName("PollNREvent: Normal Conditions")
    @Test
    void pollNREvent1() throws IOException {
        AssignClientWithEmptyProcessWaiter();
        
        when(scannerMock.nextLine()).thenReturn("A", "B", "C");
        assertEquals(pythonNRClient.pollNREvent(), "A");
        assertEquals(pythonNRClient.pollNREvent(), "B");
        assertEquals(pythonNRClient.pollNREvent(), "C");
        verify(scannerMock, times(3)).nextLine();
    }
    
    @DisplayName("PollNREvent: IOException return null")
    @Test
    void pollNREvent2() throws IOException {
        AssignClientWithEmptyProcessWaiter();
        
        when(scannerMock.nextLine()).thenReturn("A").thenThrow(new NoSuchElementException());
        assertEquals(pythonNRClient.pollNREvent(), "A");
        assertNull(pythonNRClient.pollNREvent());
        verify(scannerMock, times(2)).nextLine();
    }
    
    @DisplayName("PollNREvent: IOException return null + IllegalStateException")
    @Test
    void pollNREvent3() throws IOException {
        AssignClientWithEmptyProcessWaiter();
        
        when(scannerMock.nextLine()).thenThrow(new NoSuchElementException());
        assertNull(pythonNRClient.pollNREvent());
        assertThrows(IllegalStateException.class, () -> pythonNRClient.pollNREvent());
        verify(scannerMock, times(1)).nextLine();
    }
    
    @DisplayName("PollNREvent: IllegalStateException after disconnect")
    @Test
    void pollNREvent4() throws IOException {
        AssignClientWithEmptyProcessWaiter();
        
        when(scannerMock.nextLine()).thenReturn("A");
        pythonNRClient.disconnect();
        assertThrows(IllegalStateException.class, () -> pythonNRClient.pollNREvent());
        verify(scannerMock, never()).nextLine();
    }
    
    
    
    
    
    @DisplayName("checkError: Normal Conditions")
    @Test
    void checkError1() throws IOException {
        AssignClientWithEmptyProcessWaiter();
        
        when(errInMock.available()).thenReturn(2);
        
        when(errInMock.read()).thenAnswer(invocationOnMock -> {
            when(errInMock.available()).thenReturn(1);
            return (int) 'A';
        }).thenAnswer(invocationOnMock -> {
            when(errInMock.available()).thenReturn(0);
            return (int) 'B';
        });
        assertEquals("AB", pythonNRClient.checkError());
        verify(errInMock, times(2)).read();
        verify(errInMock, times(3)).available();
    }
    
    @DisplayName("checkError: IOException on .available() return null + IllegalStateException")
    @Test
    void checkError2() throws IOException {
        AssignClientWithEmptyProcessWaiter();
        
        when(errInMock.available()).thenReturn(1);
        
        when(errInMock.read()).thenAnswer(invocationOnMock -> {
            when(errInMock.available()).thenReturn(0);
            return (int) 'A';
        });
        
        assertEquals("A", pythonNRClient.checkError());
        
        when(errInMock.available()).thenThrow(new IOException());
        assertNull(pythonNRClient.checkError());
        assertThrows(IllegalStateException.class, () -> pythonNRClient.checkError());
        
        verify(errInMock, times(1)).read();
        
    }
    
    @DisplayName("checkError: IOException on .read() return null + IllegalStateException")
    @Test
    void checkError3() throws IOException {
        AssignClientWithEmptyProcessWaiter();
        
        when(errInMock.available()).thenReturn(1);
        
        when(errInMock.read()).thenThrow(new IOException());
        assertNull(pythonNRClient.checkError());
        assertThrows(IllegalStateException.class, () -> {pythonNRClient.checkError();});
        
        verify(errInMock, times(1)).read();
    }
    
    
    
    @DisplayName("Disconnect causes isAlive return false")
    @Test
    void disconnect1() throws IOException {
        AssignClientWithEmptyProcessWaiter();
        
        pythonNRClient.disconnect();
        assertFalse(pythonNRClient.isAlive());
    }
    
    @DisplayName("Disconnect() interrupts processWaiter")
    @Test
    void disconnect2() throws IOException {
        AssignClientWithEmptyProcessWaiter();
        
        pythonNRClient.disconnect();
        
        verify(processWaiterMock, times(1)).interrupt();
    }
    
    @DisplayName("Disconnected At returns reasonable time")
    @Test
    void disconnectedAt() throws IOException {
        AssignClientWithEmptyProcessWaiter();
        
        long realTime = System.currentTimeMillis();
        pythonNRClient.disconnect();
        
        long returnedTime = pythonNRClient.disconnectedAt();
        
        assertTrue(returnedTime >= realTime);
        assertTrue(returnedTime-realTime < 1000);
    }
    
    @DisplayName("IsAlive(): returns true")
    @Test
    void isAlive1() throws IOException {
        AssignClientWithEmptyProcessWaiter();
        
        assertTrue(pythonNRClient.isAlive());
    }
    
    @DisplayName("IsAlive(): returns false after disconnect()")
    @Test
    void isAlive() throws IOException {
        AssignClientWithEmptyProcessWaiter();
        
        pythonNRClient.disconnect();
        assertFalse(pythonNRClient.isAlive());
    }
    
    private void AssignClientWithEmptyProcessWaiter() throws IOException {
        pythonNRClient = new PythonNRClient() {
            @Override
            protected Process createPythonClientProcess() throws IOException {
                return processMock;
            }
            @Override
            protected Scanner createScanner(InputStream inputStream) {
                return scannerMock;
            }
            @Override
            protected Thread createProcessWaiterThread() {
                return processWaiterMock;
            }
        };
    }
}