package com.example.dpmjinfo;

import java.io.Serializable;

public class BusStopDeparture implements Serializable, Comparable<BusStopDeparture> {
    private String line;
    private String name;
    private String departure;
    private int lineId;
    private int connectionId;
    private int stopID;

    public BusStopDeparture(){
        this.line = "";
        this.name = "";
        this.departure = "";
        this.connectionId = -1;
        this.lineId = -1;
        this.stopID = -1;
    }

    public BusStopDeparture(String line, String name, String departure) {
        this.line = line;
        this.name = name;
        this.departure = departure;
    }

    public BusStopDeparture(String line, String name, String departure, int lId, int conId) {
        this.line = line;
        this.name = name;
        this.departure = departure;
        this.connectionId = conId;
        this.lineId = lId;
    }

    public BusStopDeparture(String line, String name, String departure, int lId, int conId, int stopID) {
        this.line = line;
        this.name = name;
        this.departure = departure;
        this.connectionId = conId;
        this.lineId = lId;
        this.stopID = stopID;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeparture() {
        return departure;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public int getLineId() {
        return lineId;
    }

    public void setLineId(int lineId) {
        this.lineId = lineId;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public int getStopID() {
        return stopID;
    }

    public void setStopID(int stopID) {
        this.stopID = stopID;
    }

    @Override
    public int compareTo(BusStopDeparture o) {
        /*if (getDeparture().equals(o.getDeparture()) &&
                getName().equals(o.getName()) &&
                getLineId() == o.getLineId() &&
                getConnectionId() == o.getConnectionId() &&
                getLine().equals(o.getLine())
        ) {

        }*/

        if(!getDeparture().equals(o.getDeparture())) return getDeparture().compareTo(o.getDeparture());
        if(!getLine().equals(o.getLine())) return getLine().compareTo(o.getLine());
        //if(!getName().equals(o.getName())) return getName().compareTo(o.getName());
        if(getLineId() != o.getLineId()) return getLineId() < o.getLineId() ? -1 : 1;
        if(getConnectionId() != o.getConnectionId()) return getConnectionId() < o.getConnectionId() ? -1 : 1;

        return 0;
    }

    public boolean isSameDepartureButWithTerminalStopName(BusStopDeparture o) {
        if(!getDeparture().equals(o.getDeparture())) return false;
        if(!getLine().equals(o.getLine())) return false;
        if(getLineId() != o.getLineId()) return false;
        if(getConnectionId() != o.getConnectionId()) return false;

        return true;
    }
}
