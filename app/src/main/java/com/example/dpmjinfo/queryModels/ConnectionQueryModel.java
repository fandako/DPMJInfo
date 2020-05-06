package com.example.dpmjinfo.queryModels;

import com.example.dpmjinfo.BusStop;

public class ConnectionQueryModel extends ScheduleQueryModel {
    private BusStop startStop;
    private BusStop targetStop;
    private String date;
    private String time;
    private int pageSize;

    public ConnectionQueryModel() {
        super();
    }

    public BusStop getStartStop() {
        return startStop;
    }

    public void setStartStop(BusStop startStop) {
        this.startStop = startStop;
    }

    public BusStop getTargetStop() {
        return targetStop;
    }

    public void setTargetStop(BusStop targetStop) {
        this.targetStop = targetStop;
    }

    public int getStartStopId() {
        return getStartStop().getCISId();
    }

    public int getTargetStopId() {
        return getTargetStop().getCISId();
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

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
