package aradnezami.cambridgesignallingmap;

import aradnezami.cambridgesignallingmap.Diagram.Point;
import aradnezami.cambridgesignallingmap.Diagram.RouteIndicator;
import aradnezami.cambridgesignallingmap.Diagram.Signal;
import aradnezami.cambridgesignallingmap.Diagram.TrackCircuit;
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
        Event[] actual = sClassDecoder.SClassChange(-1L, 0, 1);
        assertEquals(
                new Event(-1L, TrackCircuit.TYPE, TrackCircuit.OCCUPIED, "TrackCircuit"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 0, 0);
        assertEquals(
                new Event(-1L, TrackCircuit.TYPE, TrackCircuit.UNOCCUPIED, "TrackCircuit"),
                actual[0]
        );
        assertEquals(1, actual.length);
    }


    @Test
    @DisplayName("Route Indicator")
    void RouteIndicator() {
        Event[] actual = sClassDecoder.SClassChange(-1L, 0, 2);
        assertEquals(
                new Event(-1L, RouteIndicator.TYPE, RouteIndicator.PRESSED, "RouteIndicator"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 0, 0);
        assertEquals(
                new Event(-1L, RouteIndicator.TYPE, RouteIndicator.RELEASED, "RouteIndicator"),
                actual[0]
        );
        assertEquals(1, actual.length);
    }


    @Test
    @DisplayName("DGK")
    void Signal1() {
        Event[] actual = sClassDecoder.SClassChange(-1L, 0, 4);
        assertEquals(
                new Event(-1L, Signal.TYPE, Signal.CLEAR, "DGsignal"),
                actual[0]
        );
        assertEquals(1, actual.length);


        actual = sClassDecoder.SClassChange(-1L, 0, 0);
        assertEquals(
                new Event(-1L, Signal.TYPE, Signal.RESTRICTIVE, "DGsignal"),
                actual[0]
        );
        assertEquals(1, actual.length);
    }

    @Test
    @DisplayName("OFFK")
    void Signal2() {
        Event[] actual = sClassDecoder.SClassChange(-1L, 0, 8);
        assertEquals(
                new Event(-1L, Signal.TYPE, Signal.OFF, "OFFsignal"),
                actual[0]
        );
        assertEquals(1, actual.length);


        actual = sClassDecoder.SClassChange(-1L, 0, 0);
        assertEquals(
                new Event(-1L, Signal.TYPE, Signal.DANGER, "OFFsignal"),
                actual[0]
        );
        assertEquals(1, actual.length);
    }

    @Test
    @DisplayName("RGK")
    void Signal3() {
        Event[] actual = sClassDecoder.SClassChange(-1L, 0, 16);
        assertEquals(
                new Event(-1L, Signal.TYPE, Signal.DANGER, "RGsignal"),
                actual[0]
        );
        assertEquals(1, actual.length);


        actual = sClassDecoder.SClassChange(-1L, 0, 0);
        assertEquals(
                new Event(-1L, Signal.TYPE, Signal.OFF, "RGsignal"),
                actual[0]
        );
        assertEquals(1, actual.length);
    }


    @Test
    @DisplayName("Implicitly back-referenced NK+RK")
    void Points1() {
        Event[] actual = sClassDecoder.SClassChange(-1L, 0, 32);
        assertEquals(
                new Event(-1L, Point.TYPE, Point.NORMAL, "Point1"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 0, 96);
        assertEquals(
                new Event(-1L, Point.TYPE, Point.BOTH, "Point1"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 0, 64);
        assertEquals(
                new Event(-1L, Point.TYPE, Point.REVERSE, "Point1"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 0, 0);
        assertEquals(
                new Event(-1L, Point.TYPE, Point.NEITHER, "Point1"),
                actual[0]
        );
        assertEquals(1, actual.length);

    }


    @Test
    @DisplayName("Explicitly back-referenced NK+RK")
    void Points2() {
        Event[] actual = sClassDecoder.SClassChange(-1L, 0, 128);
        assertEquals(
                new Event(-1L, Point.TYPE, Point.NORMAL, "Point2"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 1, 1);
        assertEquals(
                new Event(-1L, Point.TYPE, Point.BOTH, "Point2"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 0, 0);
        assertEquals(
                new Event(-1L, Point.TYPE, Point.REVERSE, "Point2"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 1, 0);
        assertEquals(
                new Event(-1L, Point.TYPE, Point.NEITHER, "Point2"),
                actual[0]
        );
        assertEquals(1, actual.length);
    }




    @Test
    @DisplayName("Unmapped NK")
    void Points3() {
        Event[] actual = sClassDecoder.SClassChange(-1L, 1, 2);
        assertEquals(
                new Event(-1L, Point.TYPE, Point.NORMAL, "Point3"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 1, 0);
        assertEquals(
                new Event(-1L, Point.TYPE, Point.REVERSE, "Point3"),
                actual[0]
        );
        assertEquals(1, actual.length);
    }


    @Test
    @DisplayName("Unmapped RK")
    void Points4() {
        Event[] actual = sClassDecoder.SClassChange(-1L, 1, 4);
        assertEquals(
                new Event(-1L, Point.TYPE, Point.REVERSE, "Point4"),
                actual[0]
        );
        assertEquals(1, actual.length);

        actual = sClassDecoder.SClassChange(-1L, 1, 0);
        assertEquals(
                new Event(-1L, Point.TYPE, Point.NORMAL, "Point4"),
                actual[0]
        );
        assertEquals(1, actual.length);
    }

}