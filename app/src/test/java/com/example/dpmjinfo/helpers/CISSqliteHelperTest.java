package com.example.dpmjinfo.helpers;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.dpmjinfo.BusStop;
import com.example.dpmjinfo.BusStopDeparture;
import com.example.dpmjinfo.EdgeSequence;
import com.example.dpmjinfo.Line;
import com.example.dpmjinfo.ScheduleGraphEdge;
import com.example.dpmjinfo.queries.ConnectionQuery;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class CISSqliteHelperTest {
    private CISSqliteHelper db;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();

        db = new CISSqliteHelper(getClass().getClassLoader().getResource("127.db").getPath());
    }

    @Test
    public void getLinesTest() {
        List<Line> lines = db.getLines();

        assertEquals(18, lines.size());

        HashMap<Integer, String> expectedLines = new HashMap<>();
        expectedLines.put(765101, "A");
        expectedLines.put(765102, "B");
        expectedLines.put(765104, "C");
        expectedLines.put(765105, "E");
        expectedLines.put(765106, "N");
        expectedLines.put(765107, "D");
        expectedLines.put(765108, "F");
        expectedLines.put(765003, "3");
        expectedLines.put(765004, "4");
        expectedLines.put(765005, "5");
        expectedLines.put(765007, "7");
        expectedLines.put(765008, "8");
        expectedLines.put(765010, "10");
        expectedLines.put(765012, "12");
        expectedLines.put(765031, "31");
        expectedLines.put(765032, "32");
        expectedLines.put(765041, "41");
        expectedLines.put(765042, "42");

        for (Line l : lines) {
            assertTrue(expectedLines.containsKey(l.getLineId()));
            assertEquals(expectedLines.get(l.getLineId()), l.getLineName());
        }
    }

    @Test
    public void getDeparturesTest() {
        List<BusStopDeparture> departures = db.getDepartures(55782, new String[]{"X", "4"}, "2020-05-07", "04:00", 765101, 0, 10);

        assertEquals(10, departures.size());

        HashSet<String> expectedDepartures = new HashSet<>();
        expectedDepartures.add("04:22");
        expectedDepartures.add("04:25");
        expectedDepartures.add("04:31");
        expectedDepartures.add("04:36");
        expectedDepartures.add("04:36");
        expectedDepartures.add("04:40");
        expectedDepartures.add("04:42");
        expectedDepartures.add("05:00");
        expectedDepartures.add("05:02");
        expectedDepartures.add("05:03");
        expectedDepartures.add("05:22");

        for (BusStopDeparture d : departures) {
            assertTrue(expectedDepartures.contains(d.getDeparture()));
        }
    }

    @Test
    public void getLineStopsTest() {
        List<BusStop> busStops = db.getBusStopsOfLine(765101); //line A

        assertEquals(13, busStops.size());

        HashMap<Integer, String> expectedBusStops = new HashMap<>();
        expectedBusStops.put(55782, "Dopravní podnik");
        expectedBusStops.put(55833, "Tylova");
        expectedBusStops.put(13237, "Brtnická ul.");
        expectedBusStops.put(55804, "Masarykovo nám.dolní");
        expectedBusStops.put(55805, "Masarykovo nám.horní");
        expectedBusStops.put(55783, "Dům kultury");
        expectedBusStops.put(55811, "Náměstí Svobody");
        expectedBusStops.put(50428, "Chlumova");
        expectedBusStops.put(55838, "Gorkého");
        expectedBusStops.put(55816, "Pod Ján.kopečkem");
        expectedBusStops.put(55791, "Jiřího z Poděbrad");
        expectedBusStops.put(55810, "Na Vyhlídce");
        expectedBusStops.put(55787, "Hl.nádraží ČD");

        for (BusStop b : busStops) {
            assertTrue(expectedBusStops.containsKey(b.getCISId()));
            assertEquals(expectedBusStops.get(b.getCISId()), b.getName());
        }
    }

    @Test
    public void getLineDeparturesTest() {
        List<BusStopDeparture> departures = db.getLineDepartures(765101, 3);

        assertEquals(11, departures.size());

        HashMap<String, String> expectedDepartures = new HashMap<>();
        expectedDepartures.put("Dopravní podnik", "04:22");
        expectedDepartures.put("Tylova", "04:23");
        expectedDepartures.put("Brtnická ul.", "04:24");
        expectedDepartures.put("Masarykovo nám.dolní", "04:26");
        expectedDepartures.put("Masarykovo nám.horní", "04:27");
        expectedDepartures.put("Chlumova", "04:29");
        expectedDepartures.put("Gorkého", "04:30");
        expectedDepartures.put("Pod Ján.kopečkem", "04:31");
        expectedDepartures.put("Jiřího z Poděbrad", "04:32");
        expectedDepartures.put("Na Vyhlídce", "04:33");
        expectedDepartures.put("Hl.nádraží ČD", "04:34");

        for (BusStopDeparture d : departures) {
            assertTrue(expectedDepartures.containsKey(d.getName()));
            assertEquals(expectedDepartures.get(d.getName()), d.getDeparture());
        }
    }

    @Test
    public void getScheduleGraphTest() {
        HashMap<Integer, HashMap<Integer, LinkedList<ScheduleGraphEdge>>> graph = db.getScheduleGraph("00:00", "2020-05-07", new String[]{"X", "4"});

        Integer startStop = 13247;
        assertTrue(graph.containsKey(startStop));

        HashMap<Integer, LinkedList<ScheduleGraphEdge>> targetStops = graph.get(startStop);

        List<Integer> expectedTargetStops = new ArrayList<>();
        expectedTargetStops.add(55799);
        expectedTargetStops.add(13263);
        expectedTargetStops.add(55823);

        for (Integer id : expectedTargetStops) {
            assertTrue(targetStops.containsKey(id));
        }

        //test if it contains whole edge sequence of only line A connection after which arrives at terminal after 23:40
        graph = db.getScheduleGraph("23:40", "2020-05-07", new String[]{"X", "4"});

        List<ScheduleGraphEdge> edges = new ArrayList<>();
        edges.add(new ScheduleGraphEdge(765101, 174, 55787, 55810, "23:31", "23:32"));
        edges.add(new ScheduleGraphEdge(765101, 174, 55810, 55791, "23:32", "23:33"));
        edges.add(new ScheduleGraphEdge(765101, 174, 55791, 55816, "23:33", "23:34"));
        edges.add(new ScheduleGraphEdge(765101, 174, 55816, 55838, "23:34", "23:35"));
        edges.add(new ScheduleGraphEdge(765101, 174, 55838, 50428, "23:35", "23:37"));
        edges.add(new ScheduleGraphEdge(765101, 174, 50428, 55805, "23:37", "23:39"));
        edges.add(new ScheduleGraphEdge(765101, 174, 55805, 55804, "23:39", "23:40"));
        edges.add(new ScheduleGraphEdge(765101, 174, 55804, 13237, "23:40", "23:42"));
        edges.add(new ScheduleGraphEdge(765101, 174, 13237, 55833, "23:42", "23:43"));
        edges.add(new ScheduleGraphEdge(765101, 174, 55833, 55782, "23:43", "23:44"));

        for (ScheduleGraphEdge e : edges) {
            assertTrue(graph.containsKey(e.getStartStop()));

            targetStops = graph.get(e.getStartStop());

            assertTrue(targetStops.containsKey(e.getTargetStop()));

            LinkedList<ScheduleGraphEdge> edgesBetween = targetStops.get(e.getTargetStop());

            assertEquals(1, edgesBetween.size());
            ScheduleGraphEdge edge = edgesBetween.get(0);

            assertEquals(edge.getDepartureTime(), e.getDepartureTime());
            assertEquals(edge.getArrivalTime(), e.getArrivalTime());
        }
    }

    @Test
    public void getFootConnectionsTest() {
        HashMap<Integer, HashMap<Integer, LinkedList<ScheduleGraphEdge>>> graph = db.getFootConnections();

        List<ScheduleGraphEdge> edges = new ArrayList<>();
        final int con = ConnectionQuery.FOOT_CONNECTION_LINE_ID;
        edges.add(new ScheduleGraphEdge(con, con, 55805, 55806, "", ""));
        edges.add(new ScheduleGraphEdge(con, con, 55804, 55806, "", ""));
        edges.add(new ScheduleGraphEdge(con, con, 55804, 55805, "", ""));
        edges.add(new ScheduleGraphEdge(con, con, 13236, 13237, "", ""));

        edges.add(new ScheduleGraphEdge(con, con, 55806, 55805, "", ""));
        edges.add(new ScheduleGraphEdge(con, con, 55806, 55804, "", ""));
        edges.add(new ScheduleGraphEdge(con, con, 55805, 55804, "", ""));
        edges.add(new ScheduleGraphEdge(con, con, 13237, 13236, "", ""));

        for (ScheduleGraphEdge e : edges) {
            assertTrue(graph.containsKey(e.getStartStop()));

            HashMap<Integer, LinkedList<ScheduleGraphEdge>> targetStops = graph.get(e.getStartStop());

            assertTrue(targetStops.containsKey(e.getTargetStop()));

            LinkedList<ScheduleGraphEdge> edgesBetween = targetStops.get(e.getTargetStop());

            assertEquals(1, edgesBetween.size());
        }
    }
}