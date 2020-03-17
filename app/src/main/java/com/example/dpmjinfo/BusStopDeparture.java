package com.example.dpmjinfo;

public class BusStopDeparture {
    private String line;
    private String name;
    private String departure;

    public BusStopDeparture(String line, String name, String departure) {
        this.line = line;
        this.name = name;
        this.departure = departure;
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

    public void setDeparture(String departure) {
        this.departure = departure;
    }
}
