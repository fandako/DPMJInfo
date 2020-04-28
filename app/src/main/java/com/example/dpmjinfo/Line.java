package com.example.dpmjinfo;

import androidx.annotation.NonNull;

import java.util.List;

public class Line {
    private int lineId;
    private String lineName;

    public Line(int lineId, String lineName) {
        this.lineId = lineId;
        this.lineName = lineName;
    }

    public int getLineId() {
        return lineId;
    }

    public void setLineId(int lineId) {
        this.lineId = lineId;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    @Override
    public int hashCode() {
        return getLineId();
    }

    @Override
    public boolean equals(Object other) {
        if (getLineId() == ((Line) other).getLineId()) {
            return true;
        }

        return false;
    }

    //has effect on how busStops are displayed in searchable spinner!!!
    @NonNull
    @Override
    public String toString() {
        return getLineName();
    }
}
