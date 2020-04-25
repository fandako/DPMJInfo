package com.example.dpmjinfo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CISSqliteHelper {
    private SQLiteDatabase db;

    public CISSqliteHelper(String filePath) {
        db = SQLiteDatabase.openDatabase(filePath, null, SQLiteDatabase.OPEN_READONLY);
    }

    public List<BusStopDeparture> getDepartures(int stopId, String[] connectionCodes, String date, String time, int lineID, int currentPage, int pageSize) {


        final String departureFilter = "SELECT * FROM DEPARTURES WHERE stopID=" +
                stopId
                + " AND departure<>'<' AND departure<>'|' AND departure!=''";

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

        final String departureSelection = "SELECT D.departure, D.stopID, D.lineID, D.connectionID, D.rateID, L.lineName FROM (" +
                departureFilter
                + ") D JOIN (" +
                "SELECT DISTINCT L.lineID, L.lineName, C.connectionID FROM (" +
                lineFilter
                + ") L JOIN CONNECTIONS C ON L.lineID=C.lineID LEFT JOIN TIMECODES T ON T.lineID=L.lineID AND T.connectionID=C.connectionID" +
                " WHERE " +
                timeCodeCondition
                + " AND " +
                connectionCodeCondition
                + ") L ON D.lineID=L.lineID AND D.connectionID=L.connectionID WHERE " +
                resultCondition
                + " ORDER BY D.departure ASC LIMIT " + currentPage * pageSize + ", " + pageSize;

        final String sql = "SELECT RESD.lineID, RESD.connectionID, RESD.departure, RESD.lineName, SA.stopName FROM (" +
                departureSelection
                + ") RESD " +
                "JOIN (SELECT lineID, connectionID, rateID, stopID FROM DEPARTURES WHERE arrival<>'') T ON T.lineID=RESD.lineID AND T.connectionID=RESD.connectionID " +
                "JOIN STOPS SA ON SA.stopID=T.stopID ";

        Log.d("dbg", sql);
        ArrayList<BusStopDeparture> result = new ArrayList<>();

        Cursor c = db.rawQuery(sql, new String[]{});
        while (c.moveToNext()) {
            Log.d("dbg departure", c.getString(c.getColumnIndex("departure")) + " " + c.getString(c.getColumnIndex("lineName")) + " " + c.getString(c.getColumnIndex("stopName")));
            BusStopDeparture b = new BusStopDeparture(
                    c.getString(c.getColumnIndex("lineName")),
                    c.getString(c.getColumnIndex("stopName")),
                    c.getString(c.getColumnIndex("departure")),
                    c.getInt(c.getColumnIndex("lineID")),
                    c.getInt(c.getColumnIndex("connectionID"))
            );
            result.add(b);
        }

        c.close();

        return result;
    }

    public List<BusStopDeparture> getLineDepartures(int lineId, int connectionId) {
        final String sql = "SELECT D.lineID, D.connectionID, L.lineName, D.arrival, D.departure, S.stopName " +
                "FROM DEPARTURES D " +
                "JOIN STOPS S ON S.stopID=D.stopID " +
                "JOIN LINES L ON L.lineID=D.lineID " +
                "WHERE D.lineID=" + lineId + " AND D.connectionID=" + connectionId + " AND D.departure<>'<' " +
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

    public void close() {
        db.close();
    }
}
