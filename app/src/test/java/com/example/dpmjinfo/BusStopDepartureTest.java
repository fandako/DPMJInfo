package com.example.dpmjinfo;

import org.junit.Test;

import static org.junit.Assert.*;

public class BusStopDepartureTest {

    @Test
    public void compareTo() {
        BusStopDeparture d = new BusStopDeparture("A", "Stop 1", "10:15", 103, 1, 13247);
        BusStopDeparture d2 = new BusStopDeparture("C", "Stop 2", "10:16", 103, 1, 13247);

        assertEquals(-1, d.compareTo(d2));
    }

    @Test
    public void isSameDepartureButWithTerminalStopName() {
        BusStopDeparture d = new BusStopDeparture("A", "Stop 1", "10:15", 103, 1, 13247);
        BusStopDeparture d2 = new BusStopDeparture("A", "Terminal stop", "10:15", 103, 1, 13247);

        assertTrue(d.isSameDepartureButWithTerminalStopName(d2));

    }
}