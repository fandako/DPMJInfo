package com.example.dpmjinfo;

public class ScheduleGraphEdge {
    private int lineId;
    private int connectionId;
    private int startStop;
    private int targetStop;
    private String departureTime;
    private String arrivalTime;
    private String lineName;
    private String targetStopName;
    private String startStopName;

    public ScheduleGraphEdge(){

    }

    public ScheduleGraphEdge(int lineId, int connectionId, int startStop, int targetStop, String departureTime, String arrivalTime) {
        this.lineId = lineId;
        this.connectionId = connectionId;
        this.startStop = startStop;
        this.targetStop = targetStop;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    public int getLineId() {
        return lineId;
    }

    public void setLineId(int lineId) {
        this.lineId = lineId;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public int getStartStop() {
        return startStop;
    }

    public void setStartStop(int startStop) {
        this.startStop = startStop;
    }

    public int getTargetStop() {
        return targetStop;
    }

    public void setTargetStop(int targetStop) {
        this.targetStop = targetStop;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public String getTargetStopName() {
        return targetStopName;
    }

    public void setTargetStopName(String targetStopName) {
        this.targetStopName = targetStopName;
    }

    public String getStartStopName() {
        return startStopName;
    }

    public void setStartStopName(String startStopName) {
        this.startStopName = startStopName;
    }
}
