package aradnezami.cambridgesignallingmap;

import aradnezami.cambridgesignallingmap.Diagram.Point;
import aradnezami.cambridgesignallingmap.Diagram.Signal;
import aradnezami.cambridgesignallingmap.Diagram.TrackCircuit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;

import static org.mockito.Mockito.*;


class SClassHandlerTest {
    private final HashMap<Integer, String[]> dummyMap = new HashMap<>() {{
        put(Arrays.hashCode(new short[] {0,0}), new String[]{"0","0","T", "1234"});
        put(Arrays.hashCode(new short[] {0,1}), new String[]{"0","1","B", "251"});
        
        put(Arrays.hashCode(new short[] {0,2}), new String[]{"0","2", "NK", "456"});
        put(Arrays.hashCode(new short[] {0,3}), new String[]{"0","3", "RK", "456"});
        
        put(Arrays.hashCode(new short[] {0,4}), new String[]{"0","4","DGK", "250"});
        put(Arrays.hashCode(new short[] {0,5}), new String[]{"0","5", "OFFK", "250"});
        put(Arrays.hashCode(new short[] {0,6}), new String[]{"0","6", "RGK", "250"});
        
        put(Arrays.hashCode(new short[] {0,7}), new String[]{"0","7", "NK", "1152", "", "1", "0"});
        put(Arrays.hashCode(new short[] {1,0}), new String[]{"1","0", "RK", "1152", "", "0", "7"});
        
        put(Arrays.hashCode(new short[] {1,1}), new String[]{"1","1", "NK", "1134", "UNMAPPED"});
        put(Arrays.hashCode(new short[] {1,2}), new String[]{"1","2", "RK", "1136", "UNMAPPED"});
    }};
    
    
    private SClassHandler sClassHandler;
    
    @Mock
    private OutputHandler out;
    
    @BeforeEach
    void setupSClassHandler() throws FileNotFoundException {
        MockitoAnnotations.openMocks(this);
        
        boolean[] trueArray = new boolean[200];
        Arrays.fill(trueArray, true); // Pretend that all bytes have already been updated
        SClassHandler sClassHandlerUNSPIED = new SClassHandler(out, new short[200], trueArray) {
            @Override
            protected HashMap<Integer, String[]> loadEquipmentMap() {
                return dummyMap;
            }
        };
        
        sClassHandler = Mockito.spy(sClassHandlerUNSPIED);
    }
    
    
    @Test
    @DisplayName("Track Circuit")
    void SClassChange() {
        sClassHandler.SClassChange((short) 0, (short) 1);
        verify(out, times(1)).SClassChange(TrackCircuit.TYPE, TrackCircuit.OCCUPIED, "1234");
        verify(out, times(1)).SClassChange(anyInt(), anyInt(), anyString());
        
        reset(out);
        
        sClassHandler.SClassChange((short) 0, (short) 0);
        verify(out, times(1)).SClassChange(TrackCircuit.TYPE, TrackCircuit.UNOCCUPIED, "1234");
        verify(out, times(1)).SClassChange(anyInt(), anyInt(), anyString());
    }
    
    
    @Test
    @DisplayName("DGK")
    void SClassChange2() {
        sClassHandler.SClassChange((short) 0, (short) 16);
        verify(out, times(1)).SClassChange(Signal.TYPE, Signal.CLEAR, "250");
        verify(out, times(1)).SClassChange(anyInt(), anyInt(), anyString());
        
        reset(out);
        
        sClassHandler.SClassChange((short) 0, (short) 0);
        verify(out, times(1)).SClassChange(Signal.TYPE, Signal.RESTRICTIVE, "250");
        verify(out, times(1)).SClassChange(anyInt(), anyInt(), anyString());
    }
    
    @Test
    @DisplayName("OFFK")
    void SClassChange3() {
        sClassHandler.SClassChange((short) 0, (short) 32);
        verify(out, times(1)).SClassChange(Signal.TYPE, Signal.OFF, "250");
        verify(out, times(1)).SClassChange(anyInt(), anyInt(), anyString());
        
        reset(out);
        
        sClassHandler.SClassChange((short) 0, (short) 0);
        verify(out, times(1)).SClassChange(Signal.TYPE, Signal.DANGER, "250");
        verify(out, times(1)).SClassChange(anyInt(), anyInt(), anyString());
    }
    
    @Test
    @DisplayName("RGK")
    void SClassChange4() {
        sClassHandler.SClassChange((short) 0, (short) 64);
        verify(out, times(1)).SClassChange(Signal.TYPE, Signal.DANGER, "250");
        verify(out, times(1)).SClassChange(anyInt(), anyInt(), anyString());
        
        reset(out);
        
        sClassHandler.SClassChange((short) 0, (short) 0);
        verify(out, times(1)).SClassChange(Signal.TYPE, Signal.OFF, "250");
        verify(out, times(1)).SClassChange(anyInt(), anyInt(), anyString());
    }
    
    
    @Test
    @DisplayName("Explicitly back-referenced NK+RK")
    void SClassChange5() {
        sClassHandler.SClassChange((short) 0, (short) 128);
        verify(out, times(1)).SClassChange(Point.TYPE, Point.NORMAL, "1152");
        verify(out, times(1)).SClassChange(anyInt(), anyInt(), anyString());
        
        reset(out);
        
        sClassHandler.SClassChange((short) 1, (short) 1);
        verify(out, times(1)).SClassChange(Point.TYPE, Point.BOTH, "1152");
        verify(out, times(1)).SClassChange(anyInt(), anyInt(), anyString());
        
        reset(out);
        
        sClassHandler.SClassChange((short) 0, (short) 0);
        verify(out, times(1)).SClassChange(Point.TYPE, Point.REVERSE, "1152");
        verify(out, times(1)).SClassChange(anyInt(), anyInt(), anyString());
        
        reset(out);
        
        sClassHandler.SClassChange((short) 1, (short) 0);
        verify(out, times(1)).SClassChange(Point.TYPE, Point.NEITHER, "1152");
        verify(out, times(1)).SClassChange(anyInt(), anyInt(), anyString());
    }
    
    @Test
    @DisplayName("Implicitly back-referenced NK+RK")
    void SClassChange6() {
        sClassHandler.SClassChange((short) 0, (short) 4);
        verify(out, times(1)).SClassChange(Point.TYPE, Point.NORMAL, "456");
        verify(out, times(1)).SClassChange(anyInt(), anyInt(), anyString());
        
        reset(out);
        
        sClassHandler.SClassChange((short) 0, (short) 12);
        verify(out, times(1)).SClassChange(Point.TYPE, Point.BOTH, "456");
        verify(out, times(1)).SClassChange(anyInt(), anyInt(), anyString());
        
        reset(out);
        
        sClassHandler.SClassChange((short) 0, (short) 8);
        verify(out, times(1)).SClassChange(Point.TYPE, Point.REVERSE, "456");
        verify(out, times(1)).SClassChange(anyInt(), anyInt(), anyString());
        
        reset(out);
        
        sClassHandler.SClassChange((short) 0, (short) 0);
        verify(out, times(1)).SClassChange(Point.TYPE, Point.NEITHER, "456");
        verify(out, times(1)).SClassChange(anyInt(), anyInt(), anyString());
        
    }
    
    @Test
    @DisplayName("Unmapped NK")
    void SClassChange7() {
        sClassHandler.SClassChange((short) 1, (short) 2);
        verify(out, times(1)).SClassChange(Point.TYPE, Point.NORMAL, "1134");
        verify(out, times(1)).SClassChange(anyInt(), anyInt(), anyString());
        
        reset(out);
        
        sClassHandler.SClassChange((short) 1, (short) 0);
        verify(out, times(1)).SClassChange(Point.TYPE, Point.REVERSE, "1134");
        verify(out, times(1)).SClassChange(anyInt(), anyInt(), anyString());
    }
    
    @Test
    @DisplayName("Unmapped RK")
    void SClassChange8() {
        sClassHandler.SClassChange((short) 1, (short) 4);
        verify(out, times(1)).SClassChange(Point.TYPE, Point.REVERSE, "1136");
        verify(out, times(1)).SClassChange(anyInt(), anyInt(), anyString());
        
        reset(out);
        
        sClassHandler.SClassChange((short) 1, (short) 0);
        verify(out, times(1)).SClassChange(Point.TYPE, Point.NORMAL, "1136");
        verify(out, times(1)).SClassChange(anyInt(), anyInt(), anyString());
    }
}