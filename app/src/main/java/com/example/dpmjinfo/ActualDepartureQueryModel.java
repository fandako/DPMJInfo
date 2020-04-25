package com.example.dpmjinfo;

public class ActualDepartureQueryModel extends ScheduleQueryModel {
    BusStop busStop;

    public BusStop getBusStop() {
        return busStop;
    }

    public void setBusStop(BusStop busStop) {
        this.busStop = busStop;
    }
}
