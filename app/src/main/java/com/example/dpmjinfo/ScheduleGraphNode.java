package com.example.dpmjinfo;

public class ScheduleGraphNode implements Comparable<ScheduleGraphNode> {
    private int stopId;
    private ScheduleGraphEdge precedingEdge;

    public ScheduleGraphNode(int stopId, ScheduleGraphEdge precedingEdge) {
        this.stopId = stopId;
        this.precedingEdge = precedingEdge;
    }

    public int getStopId() {
        return stopId;
    }

    public void setStopId(int stopId) {
        this.stopId = stopId;
    }

    public ScheduleGraphEdge getPrecedingEdge() {
        return precedingEdge;
    }

    public void setPrecedingEdge(ScheduleGraphEdge precedingEdge) {
        this.precedingEdge = precedingEdge;
    }

    public String getDistance() {
        return precedingEdge.getArrivalTime();
    }

    @Override
    public int hashCode() {
        return getStopId();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        ScheduleGraphNode other = (ScheduleGraphNode) obj;
        return getStopId() == other.getStopId();
    }

    @Override
    public int compareTo(ScheduleGraphNode o) {
        if(getStopId() == o.getStopId()) return 0;

        return getDistance().compareTo(o.getDistance());
    }
}
