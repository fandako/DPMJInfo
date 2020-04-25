package com.example.dpmjinfo;

public class LineDetailQueryModel extends ScheduleQueryModel {
    private int lineId;
    private int connectionId;
    private BusStopDeparture highlighted;

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

    public BusStopDeparture getHighlighted() {
        return highlighted;
    }

    public void setHighlighted(BusStopDeparture highlighted) {
        this.highlighted = highlighted;
    }
}
