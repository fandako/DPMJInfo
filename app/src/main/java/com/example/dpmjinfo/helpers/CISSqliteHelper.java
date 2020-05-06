package com.example.dpmjinfo.helpers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.dpmjinfo.BusStop;
import com.example.dpmjinfo.BusStopDeparture;
import com.example.dpmjinfo.Line;
import com.example.dpmjinfo.ScheduleGraphEdge;
import com.example.dpmjinfo.queries.ConnectionQuery;
import com.example.dpmjinfo.queries.DepartureQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * class for queries on locally stored sqlite database file with schedule (departures, lines, stops, ...)
 */
public class CISSqliteHelper {
    private SQLiteDatabase db;

    /**
     *
     * @param filePath path to schedule database file
     */
    public CISSqliteHelper(String filePath) {
        db = SQLiteDatabase.openDatabase(filePath, null, SQLiteDatabase.OPEN_READONLY);
    }

    /**
     * loads departures base on departure query parameters
     * @param stopId bus stop id
     * @param connectionCodes array of connections codes
     * @param date date
     * @param time time
     * @param lineID line id
     * @param currentPage current page number
     * @param pageSize number of items to load
     * @return list of departures in ascending order
     */
    public List<BusStopDeparture> getDepartures(int stopId, String[] connectionCodes, String date, String time, int lineID, int currentPage, int pageSize) {
        return getDepartures(stopId, connectionCodes, date, time, lineID, currentPage, pageSize, false, true, "RESD.departure ASC");
    }

    /*public List<BusStopDeparture> getDepartures(String[] connectionCodes, String date, String time) {
        return getDepartures(-1, connectionCodes, date, time, -1, -1, -1, true, false, "RESD.lineID ASC, RESD.connectionID ASC, RESD.rateID ASC");
    }*/

    /**
     *
     * @return list of all bus stops
     */
    public List<BusStop> getBusStops() {
        final String sql = "SELECT \n" +
                "  S.stopID, " +
                "  S.stopName, " +
                "  C.lineCount " +
                "FROM " +
                "  STOPS S " +
                "  JOIN(" +
                "    SELECT " +
                "      stopID, " +
                "      count(lineID) AS lineCount " +
                "    FROM " +
                "      LINESTOPS " +
                "    GROUP BY " +
                "      stopID" +
                "  ) C ON C.stopID = S.stopID " +
                "ORDER BY " +
                "  C.lineCount DESC";

        ArrayList<BusStop> result = new ArrayList<>();

        Cursor c = db.rawQuery(sql, new String[]{});
        while (c.moveToNext()) {
            int id = c.getInt(c.getColumnIndex("stopID"));
            String name = c.getString(c.getColumnIndex("stopName"));
            BusStop b = new BusStop(id, name);

            result.add(b);
        }

        c.close();

        return result;
    }

    /**
     * loads stops of given line
     * @param linId line id
     * @return list of stops associated with given line
     */
    public List<BusStop> getBusStopsOfLine(Integer linId) {
        final String sql = "SELECT S.stopID, S.stopName FROM LINESTOPS L INNER JOIN STOPS S ON L.stopID=S.stopID WHERE L.lineID=?";

        Cursor c;
        if (linId != DepartureQuery.ALL_LINES) {
            c = db.rawQuery(sql, new String[]{linId.toString()});
        } else {
            return getBusStops();
        }

        ArrayList<BusStop> result = new ArrayList<>();

        while (c.moveToNext()) {
            int id = c.getInt(c.getColumnIndex("stopID"));
            String name = c.getString(c.getColumnIndex("stopName"));
            BusStop b = new BusStop(id, name);

            result.add(b);
        }

        c.close();

        return result;
    }

    /**
     * loads lines
     * @return list of lines
     */
    public List<Line> getLines() {
        final String sql = "SELECT " +
                "  L.lineID, " +
                "  L.lineName " +
                "FROM " +
                "  LINES L " +
                "  JOIN (" +
                "    SELECT " +
                "      lineID, " +
                "      count(connectionID) AS connectionCount " +
                "    FROM " +
                "      DEPARTURES " +
                "    GROUP BY " +
                "      lineID" +
                "  ) CC ON CC.lineID = L.lineID " +
                "ORDER BY " +
                "  CC.connectionCount DESC";

        ArrayList<Line> result = new ArrayList<>();

        Cursor c = db.rawQuery(sql, new String[]{});
        while (c.moveToNext()) {
            int id = c.getInt(c.getColumnIndex("lineID"));
            String name = c.getString(c.getColumnIndex("lineName"));
            Line l = new Line(id, name);

            result.add(l);
        }

        c.close();

        return result;
    }

    /**
     * loads departures base on supplied parameters
     * @param stopId stop id
     * @param connectionCodes array of connection codes
     * @param date date
     * @param time time
     * @param lineID line id
     * @param currentPage current page number
     * @param pageSize number of items to load
     * @param special load with special departure times i.e. '<' and '|' if set to true
     * @param terminal id true each departure will have terminal stop associated with it otherwise stop of given departure will be associated with it
     * @param orderBy order by clause
     * @return
     */
    public List<BusStopDeparture> getDepartures(int stopId, String[] connectionCodes, String date, String time, int lineID, int currentPage, int pageSize, boolean special, boolean terminal, String orderBy) {


        String departureFilter = "SELECT * FROM DEPARTURES ";
        String departureFilterCondition = "";

        if (stopId != -1 || !special) departureFilterCondition += "WHERE ";
        if (stopId != -1) {
            departureFilterCondition += "stopID=" + stopId;
            if (!special) departureFilterCondition += " AND ";
        }

        if (!special) {
            departureFilterCondition += "departure<>'<' AND departure<>'|' AND departure!=''";
        }

        departureFilter += departureFilterCondition;

        String lineFilter = "SELECT lineID, lineName FROM LINES WHERE validFrom <= '" +
                date
                + "' AND validTo >= '" +
                date
                + "'";

        if (lineID != -1) {
            lineFilter = lineFilter + " AND lineID=" + lineID;
        }

        StringBuilder connectionCodeCondition = new StringBuilder("(");
        String codeCondition = "";
        for (int i = 0; i < connectionCodes.length; i++) {
            String code = connectionCodes[i];
            codeCondition = "(C.code1='" + code
                    + "' OR C.code2='" + code
                    + "' OR C.code3='" + code
                    + "' OR C.code4='" + code
                    + "' OR C.code5='" + code
                    + "' OR C.code6='" + code
                    + "' OR C.code7='" + code
                    + "' OR C.code8='" + code
                    + "' OR C.code9='" + code
                    + "' OR C.code10='" + code
                    + "')";
            if (i < connectionCodes.length - 1) {
                connectionCodeCondition.append(codeCondition).append(" OR ");
            }
        }
        connectionCodeCondition.append(codeCondition).append(")");

        //String connectionCodeCondition = "((C.code1='00002' OR C.code2='00002' OR C.code3='00002' OR C.code4='00002' OR C.code5='00002' OR C.code6='00002' OR C.code7='00002' OR C.code8='00002' OR C.code9='00002' OR C.code10='00002') " +
        //        "OR (C.code1='00009' OR C.code2='00009' OR C.code3='00009' OR C.code4='00009' OR C.code5='00009' OR C.code6='00009' OR C.code7='00009' OR C.code8='00009' OR C.code9='00009' OR C.code10='00009'))";

        final String timeCodeCondition = "((T.timeCodeID IS NULL) OR (T.timeCodeType=1 AND T.dateFrom<='" +
                date
                + "' AND T.dateTo>='" +
                date
                + "') OR (T.timeCodeType=4 AND (T.dateFrom>'" +
                date
                + "' OR T.dateTo<'" +
                date
                + "')))";

        final String resultCondition = "D.departure >= '" + time + "'";

        //uses join to only return connections which have some departure after the given time
        final String connectionFilterForGivenTime = "JOIN (SELECT DISTINCT lineID, connectionID FROM DEPARTURES WHERE departure<>'<' AND departure<>'|' AND departure<>'' AND departure>='" + time + "') D2 " +
                "ON D.lineID=D2.lineID AND D.connectionID=D2.connectionID";

        String departureSelection = "SELECT D.departure, D.stopID, D.lineID, D.connectionID, D.rateID, L.lineName, D.arrival FROM (" +
                departureFilter
                + ") D JOIN (" +
                "SELECT DISTINCT L.lineID, L.lineName, C.connectionID FROM (" +
                lineFilter
                + ") L JOIN CONNECTIONS C ON L.lineID=C.lineID LEFT JOIN TIMECODES T ON T.lineID=L.lineID AND T.connectionID=C.connectionID" +
                " WHERE " +
                timeCodeCondition
                + " AND " +
                connectionCodeCondition
                + ") L ON D.lineID=L.lineID AND D.connectionID=L.connectionID " + /*WHERE " +*/
                connectionFilterForGivenTime;

        //if special symbols like < and | are present, filtration on time would not work
        if (!special) {
            departureSelection += " WHERE " + resultCondition + " ORDER BY D.departure ASC ";
        }


        if (pageSize != -1) {
            departureSelection += "LIMIT " + currentPage * pageSize + ", " + pageSize;
        }
        String sql = "SELECT RESD.lineID, RESD.connectionID, RESD.departure, RESD.lineName, SA.stopName, SA.stopID, RESD.arrival FROM (" +
                departureSelection
                + ") RESD " +
                "JOIN (SELECT lineID, connectionID, rateID, stopID FROM DEPARTURES WHERE arrival<>'') T ON T.lineID=RESD.lineID AND T.connectionID=RESD.connectionID " +
                "JOIN STOPS SA ON ";

        if (terminal) {
            sql += "SA.stopID=T.stopID";
        } else {
            sql += "SA.stopID=RESD.stopID";
        }

        if (!orderBy.equals("")) {
            sql += " ORDER BY " + orderBy; //" ORDER BY D.departure ASC ";
        }

        Log.d("dbg", sql);
        ArrayList<BusStopDeparture> result = new ArrayList<>();

        Cursor c = db.rawQuery(sql, new String[]{});
        while (c.moveToNext()) {
            //Log.d("dbg departure", c.getString(c.getColumnIndex("departure")) + " " + c.getString(c.getColumnIndex("lineName")) + " " + c.getString(c.getColumnIndex("stopName")));
            String departure = c.getString(c.getColumnIndex("departure"));

            //terminal stop does not have departure time, only arrival time
            if (departure.equals("")) {
                departure = c.getString(c.getColumnIndex("arrival"));
            }

            BusStopDeparture b = new BusStopDeparture(
                    c.getString(c.getColumnIndex("lineName")),
                    c.getString(c.getColumnIndex("stopName")),
                    departure,
                    c.getInt(c.getColumnIndex("lineID")),
                    c.getInt(c.getColumnIndex("connectionID")),
                    c.getInt(c.getColumnIndex("stopID"))
            );
            result.add(b);
        }

        c.close();

        return result;
    }

    /**
     * loads departure for given line connection
     * @param lineId line id
     * @param connectionId connection id
     * @return list of departures in order from start to terminal stop
     */
    public List<BusStopDeparture> getLineDepartures(int lineId, int connectionId) {
        final String sql = "SELECT D.lineID, D.connectionID, L.lineName, D.arrival, D.departure, S.stopName " +
                "FROM DEPARTURES D " +
                "JOIN STOPS S ON S.stopID=D.stopID " +
                "JOIN LINES L ON L.lineID=D.lineID " +
                "WHERE D.lineID=" + lineId + " AND D.connectionID=" + connectionId + " AND D.departure<>'<'  AND D.departure<>'|'" +
                "ORDER BY D.arrival ASC, D.departure ASC";

        ArrayList<BusStopDeparture> result = new ArrayList<>();

        Cursor c = db.rawQuery(sql, new String[]{});
        while (c.moveToNext()) {
            String departure = c.getString(c.getColumnIndex("departure"));

            //terminal stop does not have departure time, only arrival time
            if (departure.equals("")) {
                departure = c.getString(c.getColumnIndex("arrival"));
            }

            BusStopDeparture b = new BusStopDeparture(
                    c.getString(c.getColumnIndex("lineName")),
                    c.getString(c.getColumnIndex("stopName")),
                    departure,
                    c.getInt(c.getColumnIndex("lineID")),
                    c.getInt(c.getColumnIndex("connectionID"))
            );
            result.add(b);
        }

        c.close();

        return result;
    }

    /**
     * loads departures preprocessed for easier and more efficient loading for connection queries
     * @param time time
     * @param date date
     * @param connectionCodes array of connection codes
     * @return list od departures
     */
    private List<BusStopDeparture> getScheduleGraphDepartures(String time, String date, String[] connectionCodes) {
        long startTime = System.currentTimeMillis();

        StringBuilder connectionCodeCondition = new StringBuilder("(");
        String codeCondition = "";
        Log.d("dbg codes", "" + connectionCodes.length);
        for (int i = 0; i < connectionCodes.length; i++) {
            String code = connectionCodes[i];
            codeCondition = "(code1='" + code
                    + "' OR code2='" + code
                    + "' OR code3='" + code
                    + "' OR code4='" + code
                    + "' OR code5='" + code
                    + "' OR code6='" + code
                    + "' OR code7='" + code
                    + "' OR code8='" + code
                    + "' OR code9='" + code
                    + "' OR code10='" + code
                    + "')";
            if (i < connectionCodes.length - 1) {
                connectionCodeCondition.append(codeCondition).append(" OR ");
            }
        }
        connectionCodeCondition.append(codeCondition).append(")");

        String sql = "SELECT " +
                "    lineID," +
                "    connectionID," +
                "    rateID," +
                "    arrival," +
                "    departure," +
                "    stopID," +
                "    stopName," +
                "    lineName " +
                "FROM " +
                "    graphDepartures " +
                "WHERE " +
                "    (" +
                "        (timeCodeType IS NULL) " +
                "        OR (" +
                "            timeCodeType = 1 " +
                "            AND dateFrom <= '" + date + "'" +
                "            AND dateTo >= '" + date + "'" +
                "        )" +
                "        OR (" +
                "            timeCodeType = 4 " +
                "            AND (" +
                "                dateFrom > '" + date + "'" +
                "                OR dateTo < '" + date + "'" +
                "            )" +
                "        )" +
                "    )" +
                "    AND " +
                connectionCodeCondition +
                "    AND terminalArrival >= '" + time + "' " +
                "ORDER BY " +
                "    lineID ASC," +
                "    connectionID ASC," +
                "    rateID ASC";
        Log.d("dbg", sql);
        ArrayList<BusStopDeparture> result = new ArrayList<>();

        Cursor c = db.rawQuery(sql, new String[]{});
        while (c.moveToNext()) {
            //Log.d("dbg departure", c.getString(c.getColumnIndex("departure")) + " " + c.getString(c.getColumnIndex("lineName")) + " " + c.getString(c.getColumnIndex("stopName")));
            String departure = c.getString(c.getColumnIndex("departure"));

            //terminal stop does not have departure time, only arrival time
            if (departure.equals("")) {
                departure = c.getString(c.getColumnIndex("arrival"));
            }

            BusStopDeparture b = new BusStopDeparture(
                    c.getString(c.getColumnIndex("lineName")),
                    c.getString(c.getColumnIndex("stopName")),
                    departure,
                    c.getInt(c.getColumnIndex("lineID")),
                    c.getInt(c.getColumnIndex("connectionID")),
                    c.getInt(c.getColumnIndex("stopID"))
            );
            result.add(b);
        }

        c.close();

        long endTime = System.currentTimeMillis();
        Log.d("dbg time select", (endTime - startTime) + " ms");

        return result;
    }

    /**
     * loads departures for connection query and returns them as structure for efficient adjacency lookup
     * @param time time
     * @param date date
     * @param connectionCodes array of connection codes
     * @return HashMap<start stop id, HashMap<target stop id, LinkedList<ScheduleGraphEdge>>>
     */
    public HashMap<Integer, HashMap<Integer, LinkedList<ScheduleGraphEdge>>> getScheduleGraph(String time, String date, String[] connectionCodes) {
        List<BusStopDeparture> departures = getScheduleGraphDepartures(time, date, connectionCodes);//getDepartures(connectionCodes, date, time);

        HashMap<Integer, HashMap<Integer, LinkedList<ScheduleGraphEdge>>> result = new HashMap<>();

        long startTime = System.currentTimeMillis();

        Iterator<BusStopDeparture> i = departures.iterator();
        if (i.hasNext()) {
            BusStopDeparture d = i.next();

            // < | -> vehicle rides on different line - does not stop on these stops
            while (d.getDeparture().equals("<") || d.getDeparture().equals("|")) {
                d = i.next();
            }

            BusStopDeparture nextD;
            do {
                nextD = i.next();

                if (!(d.getConnectionId() == nextD.getConnectionId() && d.getLineId() == nextD.getLineId())) {
                    d = nextD;
                    nextD = i.next();
                }

                // < | -> vehicle rides on different line - does not stop on these stops
                while (nextD.getDeparture().equals("<") || nextD.getDeparture().equals("|")) {
                    nextD = i.next();
                }

                ScheduleGraphEdge e = new ScheduleGraphEdge();

                //odd connections are in opposite order
                if (d.getDeparture().compareTo(nextD.getDeparture()) > 0) {
                    e.setStartStop(nextD.getStopID());
                    e.setDepartureTime(nextD.getDeparture());
                    e.setStartStopName(nextD.getName());
                    e.setTargetStop(d.getStopID());
                    e.setArrivalTime(d.getDeparture());
                    e.setTargetStopName(d.getName());
                } else {
                    e.setStartStop(d.getStopID());
                    e.setDepartureTime(d.getDeparture());
                    e.setStartStopName(d.getName());
                    e.setTargetStop(nextD.getStopID());
                    e.setArrivalTime(nextD.getDeparture());
                    e.setTargetStopName(nextD.getName());
                }


                e.setLineId(d.getLineId());
                e.setConnectionId(d.getConnectionId());
                e.setLineName(d.getLine());

                HashMap<Integer, LinkedList<ScheduleGraphEdge>> inner;

                Integer outerKey = e.getStartStop();
                if (result.containsKey(outerKey)) {
                    inner = result.get(outerKey);
                } else {
                    inner = new HashMap<>();
                    result.put(outerKey, inner);
                }

                Integer innerKey = e.getTargetStop();
                if (inner.containsKey(innerKey)) {
                    inner.get(innerKey).add(e);
                } else {
                    LinkedList<ScheduleGraphEdge> edgeList = new LinkedList<>();
                    edgeList.add(e);

                    inner.put(innerKey, edgeList);
                }

                d = nextD;
            } while (i.hasNext());
        }



        for (int key: result.keySet()) {
            for (int innerKey: result.get(key).keySet()) {
                Collections.sort(result.get(key).get(innerKey), new Comparator<ScheduleGraphEdge>() {
                    @Override
                    public int compare(ScheduleGraphEdge o1, ScheduleGraphEdge o2) {
                        return o1.getDepartureTime().compareTo(o2.getDepartureTime());
                    }
                });
            }
        }

        long endTime = System.currentTimeMillis();
        Log.d("dbg time sort", (endTime - startTime) + " ms");

        return result;
    }

    /**
     * loads foot connections
     * @return HashMap<start stop id, HashMap<target stop id, LinkedList<ScheduleGraphEdge>>>
     */
    public HashMap<Integer, HashMap<Integer, LinkedList<ScheduleGraphEdge>>> getFootConnections() {
        final String sql = "SELECT startStopId, S.stopName AS startStopName, targetStopId, T.stopName AS targetStopName, time FROM FOOTCONNECTIONS F JOIN STOPS S ON F.startStopId=S.stopID JOIN STOPS T ON F.targetStopId=T.stopID";

        Cursor c = db.rawQuery(sql, new String[]{});

        HashMap<Integer, HashMap<Integer, LinkedList<ScheduleGraphEdge>>> result = new HashMap<>();
        List<ScheduleGraphEdge> edges = new ArrayList<>();

        while (c.moveToNext()) {
            ScheduleGraphEdge eOneWay = new ScheduleGraphEdge();
            ScheduleGraphEdge eOtherWay = new ScheduleGraphEdge();

            int startStopId = c.getInt(c.getColumnIndex("startStopId"));
            String startStopName = c.getString(c.getColumnIndex("startStopName"));
            int targetStopId = c.getInt(c.getColumnIndex("targetStopId"));
            String targetStopName = c.getString(c.getColumnIndex("targetStopName"));
            int time = c.getInt(c.getColumnIndex("time"));

            eOneWay.setStartStop(startStopId);
            eOneWay.setStartStopName(startStopName);
            eOneWay.setTargetStop(targetStopId);
            eOneWay.setTargetStopName(targetStopName);
            eOneWay.setLineId(ConnectionQuery.FOOT_CONNECTION_LINE_ID);
            eOneWay.setConnectionId(ConnectionQuery.FOOT_CONNECTION_LINE_ID);
            eOneWay.setTime(time);

            eOtherWay.setStartStop(targetStopId);
            eOtherWay.setStartStopName(targetStopName);
            eOtherWay.setTargetStop(startStopId);
            eOtherWay.setTargetStopName(startStopName);
            eOtherWay.setLineId(ConnectionQuery.FOOT_CONNECTION_LINE_ID);
            eOtherWay.setConnectionId(ConnectionQuery.FOOT_CONNECTION_LINE_ID);
            eOtherWay.setTime(time);

            edges.add(eOneWay);
            edges.add(eOtherWay);
        }

        for (ScheduleGraphEdge e:edges) {
            HashMap<Integer, LinkedList<ScheduleGraphEdge>> inner;

            Integer outerKey = e.getStartStop();
            if (result.containsKey(outerKey)) {
                inner = result.get(outerKey);
            } else {
                inner = new HashMap<>();
                result.put(outerKey, inner);
            }

            Integer innerKey = e.getTargetStop();
            if (inner.containsKey(innerKey)) {
                inner.get(innerKey).add(e);
            } else {
                LinkedList<ScheduleGraphEdge> edgeList = new LinkedList<>();
                edgeList.add(e);

                inner.put(innerKey, edgeList);
            }
        }

        //sort according to time
        for (int key: result.keySet()) {
            for (int innerKey: result.get(key).keySet()) {
                Collections.sort(result.get(key).get(innerKey), new Comparator<ScheduleGraphEdge>() {
                    @Override
                    public int compare(ScheduleGraphEdge o1, ScheduleGraphEdge o2) {
                        return Integer.compare(o1.getTime(), o2.getTime());
                    }
                });
            }
        }

        c.close();

        return result;
    }

    public void close() {
        db.close();
    }
}
