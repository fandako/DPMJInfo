package com.example.dpmjinfo;

public class ActualDepartureQueryModel extends ScheduleQueryModel {
    private BusStop busStop;

    public BusStop getBusStop() {
        return busStop;
    }

    public void setBusStop(BusStop busStop) {
        this.busStop = busStop;
    }
}
