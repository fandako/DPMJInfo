package com.example.dpmjinfo;

import com.example.dpmjinfo.BusStopDeparture;

import java.util.ArrayList;
import java.util.List;

public class Connection {
    private List<BusStopDeparture> departures;

    public Connection() {
        departures = new ArrayList<>();
    }

    public Connection(List<BusStopDeparture> departures) {
        this.departures = new ArrayList<>();
        this.departures.addAll(departures);
    }

    public void addDeparture(BusStopDeparture d) {
        departures.add(d);
    }

    public void addDepartures(List<BusStopDeparture> departures) {
        this.departures.addAll(departures);
    }

    public List<BusStopDeparture> getDepartures() {
        return departures;
    }

    public void setDepartures(List<BusStopDeparture> departures) {
        this.departures = departures;
    }

    public BusStopDeparture get(int index){
        return departures.get(index);
    }

    public int size() {
        return departures.size();
    }
}
