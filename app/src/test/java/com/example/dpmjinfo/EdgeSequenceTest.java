package com.example.dpmjinfo;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class EdgeSequenceTest {
    private ScheduleGraphEdge getTestEdge(){
        ScheduleGraphEdge e = new ScheduleGraphEdge();
        e.setStartStop(1);
        e.setStartStopName("stop 1");
        e.setTargetStop(2);
        e.setTargetStopName("stop 2");
        e.setArrivalTime("10:15");
        e.setDepartureTime("10:12");
        e.setLineId(103);
        e.setConnectionId(2);

        return e;
    }

    private ScheduleGraphEdge getTestEdge2(){
        ScheduleGraphEdge e = new ScheduleGraphEdge();
        e.setStartStop(2);
        e.setStartStopName("stop 2");
        e.setTargetStop(3);
        e.setTargetStopName("stop 3");
        e.setArrivalTime("10:19");
        e.setDepartureTime("10:17");
        e.setLineId(105);
        e.setConnectionId(1);

        return e;
    }

    @Test
    public void testContainsEdge() {
        EdgeSequence e = new EdgeSequence();
        ScheduleGraphEdge edge = getTestEdge();

        e.addEdge(edge);

        assertTrue(e.containsEdge(edge));
    }

    @Test
    public void testAddEdge() {
        EdgeSequence e = new EdgeSequence();
        ScheduleGraphEdge edge = getTestEdge();

        assertEquals(0, e.getEdges().size());

        e.addEdge(edge);

        assertEquals(1, e.getEdges().size());
    }

    @Test
    public void testGetLineCount() {
        EdgeSequence e = new EdgeSequence();
        ScheduleGraphEdge edge = getTestEdge();

        e.addEdge(edge);

        assertEquals(1, e.getLineCount());
    }

    @Test
    public void testAddAllEdge() {
        EdgeSequence e = new EdgeSequence();
        List<ScheduleGraphEdge> edges = new ArrayList<>();
        edges.add(getTestEdge());
        edges.add(getTestEdge2());

        assertEquals(0, e.getEdges().size());

        e.addAllEdge(edges);

        assertEquals(2, e.getEdges().size());

        assertTrue(e.containsEdge(getTestEdge()));
        assertTrue(e.containsEdge(getTestEdge2()));
    }

    @Test
    public void testGetWaiting() {
        EdgeSequence e = new EdgeSequence();
        List<ScheduleGraphEdge> edges = new ArrayList<>();
        edges.add(getTestEdge());
        edges.add(getTestEdge2());

        e.addAllEdge(edges);

        assertEquals(2, e.getWaiting());
    }

    @Test
    public void testEquals() {
        EdgeSequence e = new EdgeSequence();
        EdgeSequence e2 = new EdgeSequence();
        ScheduleGraphEdge edge = getTestEdge();

        e.addEdge(edge);
        e2.addEdge(edge);

        assertTrue(e.equals(e2));
    }

    @Test
    public void testCompareTo() {
        EdgeSequence e = new EdgeSequence();
        EdgeSequence e2 = new EdgeSequence();

        e.addEdge(getTestEdge());
        e2.addEdge(getTestEdge2());

        assertTrue(e.compareTo(e2) < 0);

        e2 = new EdgeSequence();
        e2.addEdge(getTestEdge());

        assertEquals(0, e.compareTo(e2));

        e.addEdge(getTestEdge2());
        e2 = new EdgeSequence();
        e2.addEdge(getTestEdge2());

        assertTrue(e.compareTo(e2) > 0);
    }

    @Test
    public void testGetLastTime() {
        EdgeSequence e = new EdgeSequence();
        List<ScheduleGraphEdge> edges = new ArrayList<>();
        edges.add(getTestEdge());
        edges.add(getTestEdge2());

        e.addAllEdge(edges);

        assertEquals("10:19", e.getLastTime());
    }
}