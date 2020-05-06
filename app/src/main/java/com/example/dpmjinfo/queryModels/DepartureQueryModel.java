package com.example.dpmjinfo.queryModels;

import com.example.dpmjinfo.BusStop;
import com.example.dpmjinfo.Line;

public class DepartureQueryModel extends ScheduleQueryModel {
    private BusStop stop;
    private String date;
    private String time;
    private Line line;
    private int pageSize;

    public DepartureQueryModel() {
        super();
    }

    public BusStop getStop() {
        return stop;
    }

    public int getStopId() {
        return getStop().getCISId();
    }

    public void setStop(BusStop stop) {
        this.stop = stop;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Line getLine() {
        return line;
    }

    public int getLineId() {
        return getLine().getLineId();
    }

    public void setLine(Line line) {
        this.line = line;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
