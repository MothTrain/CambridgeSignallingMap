package aradnezami.cambridgesignallingmap.NRFeed;

import aradnezami.cambridgesignallingmap.UI.DiagramElements.Point;
import aradnezami.cambridgesignallingmap.UI.DiagramElements.Signal;
import aradnezami.cambridgesignallingmap.UI.DiagramElements.TrackCircuit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;


class SClassDecoderTest {
    private SClassDecoder sClassDecoder;


    @BeforeEach
    void setupSClassDecoder() throws FileNotFoundException {
        sClassDecoder = new SClassDecoder("SignallingEquipmentMap.csv") {};

        for (int i=0; i<256; i++) {
            sClassDecoder.SClassChange(-1L, i, 0);
        }
    }
    
    
    @Test
    @DisplayName("Track Circuit")
    void TrackCircuit() {
        Event[] actual = sClassDecoder.SClassChange(-1L, 0, toByte("1000 0000"));
        assertEquals(
                new Event(-1L, TrackCircuit.TYPE, TrackCircuit.OCCUPIED, "TrackCircuit"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 0, toByte("0000 0000"));
        assertEquals(
                new Event(-1L, TrackCircuit.TYPE, TrackCircuit.UNOCCUPIED, "TrackCircuit"),
                actual[0]
        );
        assertEquals(1, actual.length);
    }


    @Test
    @DisplayName("Route Indicator")
    void RouteIndicator() {
        Event[] actual = sClassDecoder.SClassChange(-1L, 0, toByte("0100 0000"));
        assertEquals(
                new Event(-1L, Signal.ROUTED_TYPE, Signal.ROUTE_SET, "RouteIndicator"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 0, toByte("0000 0000"));
        assertEquals(
                new Event(-1L, Signal.ROUTED_TYPE, Signal.ROUTE_NOT_SET, "RouteIndicator"),
                actual[0]
        );
        assertEquals(1, actual.length);
    }


    @Test
    @DisplayName("DGK")
    void Signal1() {
        Event[] actual = sClassDecoder.SClassChange(-1L, 0, toByte("0010 0000"));
        assertEquals(
                new Event(-1L, Signal.ASPECT_TYPE, Signal.MAIN_OFF, "DGsignal"),
                actual[0]
        );
        assertEquals(1, actual.length);


        actual = sClassDecoder.SClassChange(-1L, 0, toByte("0000 0000"));
        assertEquals(
                new Event(-1L, Signal.ASPECT_TYPE, Signal.ON, "DGsignal"),
                actual[0]
        );
        assertEquals(1, actual.length);
    }

    @Test
    @DisplayName("OFFK")
    void Signal2() {
        Event[] actual = sClassDecoder.SClassChange(-1L, 0, toByte("0001 0000"));
        assertEquals(
                new Event(-1L, Signal.ASPECT_TYPE, Signal.MAIN_OFF, "OFFsignal"),
                actual[0]
        );
        assertEquals(1, actual.length);


        actual = sClassDecoder.SClassChange(-1L, 0, toByte("0000 0000"));
        assertEquals(
                new Event(-1L, Signal.ASPECT_TYPE, Signal.ON, "OFFsignal"),
                actual[0]
        );
        assertEquals(1, actual.length);
    }

    @Test
    @DisplayName("RGK")
    void Signal3() {
        Event[] actual = sClassDecoder.SClassChange(-1L, 0, toByte("0000 1000"));
        assertEquals(
                new Event(-1L, Signal.ASPECT_TYPE, Signal.ON, "RGsignal"),
                actual[0]
        );
        assertEquals(1, actual.length);


        actual = sClassDecoder.SClassChange(-1L, 0, toByte("0000 0000"));
        assertEquals(
                new Event(-1L, Signal.ASPECT_TYPE, Signal.MAIN_OFF, "RGsignal"),
                actual[0]
        );
        assertEquals(1, actual.length);
    }


    @Test
    @DisplayName("Implicitly back-referenced NK+RK")
    void Points1() {
        Event[] actual = sClassDecoder.SClassChange(-1L, 0, toByte("0000 0100"));
        assertEquals(
                new Event(-1L, Point.TYPE, Point.NORMAL, "Point1"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 0, toByte("0000 0110"));
        assertEquals(
                new Event(-1L, Point.TYPE, Point.BOTH, "Point1"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 0, toByte("0000 0010"));
        assertEquals(
                new Event(-1L, Point.TYPE, Point.REVERSE, "Point1"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 0, toByte("0000 0000"));
        assertEquals(
                new Event(-1L, Point.TYPE, Point.NEITHER, "Point1"),
                actual[0]
        );
        assertEquals(1, actual.length);

    }


    @Test
    @DisplayName("Explicitly back-referenced NK+RK")
    void Points2() {
        Event[] actual = sClassDecoder.SClassChange(-1L, 0, toByte("0000 0001"));
        assertEquals(
                new Event(-1L, Point.TYPE, Point.NORMAL, "Point2"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 1, toByte("1000 0000"));
        assertEquals(
                new Event(-1L, Point.TYPE, Point.BOTH, "Point2"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 0, toByte("0000 0000"));
        assertEquals(
                new Event(-1L, Point.TYPE, Point.REVERSE, "Point2"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 1, toByte("0000 0000"));
        assertEquals(
                new Event(-1L, Point.TYPE, Point.NEITHER, "Point2"),
                actual[0]
        );
        assertEquals(1, actual.length);
    }




    @Test
    @DisplayName("Unmapped NK")
    void Points3() {
        Event[] actual = sClassDecoder.SClassChange(-1L, 1, toByte("0100 0000"));
        assertEquals(
                new Event(-1L, Point.TYPE, Point.NORMAL, "Point3"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 1, toByte("0000 0000"));
        assertEquals(
                new Event(-1L, Point.TYPE, Point.REVERSE, "Point3"),
                actual[0]
        );
        assertEquals(1, actual.length);
    }


    @Test
    @DisplayName("Unmapped RK")
    void Points4() {
        Event[] actual = sClassDecoder.SClassChange(-1L, 1, toByte("0010 0000"));
        assertEquals(
                new Event(-1L, Point.TYPE, Point.REVERSE, "Point4"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 1, toByte("0000 0000"));
        assertEquals(
                new Event(-1L, Point.TYPE, Point.NORMAL, "Point4"),
                actual[0]
        );
        assertEquals(1, actual.length);
    }


    @Test
    @DisplayName("DGK+SOFFK Compound signal")
    void CompoundSignal1() {
        Event[] actual = sClassDecoder.SClassChange(-1L, 1, toByte("0000 0010"));
        assertEquals(
                new Event(-1L, Signal.ASPECT_TYPE, Signal.MAIN_OFF, "CompoundSig1"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 1, toByte("0000 0011"));
        assertEquals(
                new Event(-1L, Signal.ASPECT_TYPE, Signal.BOTH_OFF, "CompoundSig1"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 1, toByte("0000 0001"));
        assertEquals(
                new Event(-1L, Signal.ASPECT_TYPE, Signal.SHUNT_OFF, "CompoundSig1"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 1, toByte("0000 0000"));
        assertEquals(
                new Event(-1L, Signal.ASPECT_TYPE, Signal.ON, "CompoundSig1"),
                actual[0]
        );
        assertEquals(1, actual.length);
    }


    @Test
    @DisplayName("OFFK+RM Compound signal")
    void CompoundSignal2() {
        Event[] actual = sClassDecoder.SClassChange(-1L, 2, toByte("1000 0000"));
        assertEquals(
                new Event(-1L, Signal.ASPECT_TYPE, Signal.SHUNT_OFF, "CompoundSig2"),
                actual[0]
        );
        assertEquals(1, actual.length);

        sClassDecoder.SClassChange(-1L, 2, toByte("0100 0000"));

        actual = sClassDecoder.SClassChange(-1L, 2, toByte("1100 0000"));
        assertEquals(
                new Event(-1L, Signal.ASPECT_TYPE, Signal.MAIN_OFF, "CompoundSig2"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 2, toByte("0100 0000"));
        assertEquals(
                new Event(-1L, Signal.ASPECT_TYPE, Signal.ON, "CompoundSig2"),
                actual[0]
        );
        assertEquals(1, actual.length);
    }


    @Test
    void allEvents() {
        sClassDecoder.SClassChange(-1L, 0, toByte("1100 0000"));
        sClassDecoder.SClassChange(-1L, 1, toByte("0100 0000"));

        Event[] actual = sClassDecoder.allEvents();
        assertEquals(actual.length, 18);
        // We've already verified that the sClassDecoder decodes correctly, so we'll just check
        // if the correct number of events is returned
    }

    /**
     * Byte in binary format in a string (eg: 1000 1000). Spaces allowed. <b>First bit is LSB and last is MSB</b>
     */
    private int toByte(String str) {
        str = str.replaceAll(" ", "");
        char[] chars = str.toCharArray();

        int total = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '0') {continue;}

            total += (int) Math.pow(2, i);
        }

        return total;
    }

    @Test
    void reset() {
        try {
            sClassDecoder = new SClassDecoder("SignallingEquipmentMap.csv"); // We want an instance with nothing updated
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        int expected = sClassDecoder.SClassChange(-1L, 0, toByte("1111 1111")).length;
        sClassDecoder.reset();

        int actual = sClassDecoder.SClassChange(-1L, 0, toByte("1111 1111")).length;

        assertEquals(expected, actual);
    }
}