package com.example.dpmjinfo;

public class ConnectionQueryModel extends ScheduleQueryModel {
    private int startStop;
    private int targetStop;
    private String date;
    private String time;
    private int pageSize;

    public int getStartStop() {
        return startStop;
    }

    public void setStartStop(int startStop) {
        this.startStop = startStop;
    }

    public int getTargetStop() {
        return targetStop;
    }

    public void setTargetStop(int targetStop) {
        this.targetStop = targetStop;
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
