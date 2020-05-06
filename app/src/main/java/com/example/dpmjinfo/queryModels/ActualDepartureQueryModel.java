package com.example.dpmjinfo.queryModels;

import com.example.dpmjinfo.BusStop;
import com.example.dpmjinfo.queries.ActualDepartureQuery;

public class ActualDepartureQueryModel extends ScheduleQueryModel {
    private BusStop busStop;

    public ActualDepartureQueryModel() {
        super();
    }

    public BusStop getBusStop() {
        return busStop;
    }

    public void setBusStop(BusStop busStop) {
        this.busStop = busStop;
    }
}
