package com.example.dpmjinfo.queryModels;

import com.example.dpmjinfo.BusStopDeparture;

import java.util.ArrayList;
import java.util.List;

public class LineDetailQueryModel extends ScheduleQueryModel {
    private int lineId;
    private int connectionId;
    private List<BusStopDeparture> highlighted;

    public LineDetailQueryModel(){
        super();

        showAddToFavourite = false;
        highlighted = new ArrayList<>();
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

    public List<BusStopDeparture> getHighlighted() {
        return highlighted;
    }

    public void addHighlighted(BusStopDeparture highlighted) {
        this.highlighted.add(highlighted);
    }
}
