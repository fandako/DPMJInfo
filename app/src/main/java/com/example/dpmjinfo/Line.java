package com.example.dpmjinfo;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

public class Line implements Serializable {
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
        if(getClass() != other.getClass()){
            return false;
        }

        return getLineId() == ((Line) other).getLineId();
    }

    //has effect on how busStops are displayed in searchable spinner!!!
    @NonNull
    @Override
    public String toString() {
        return getLineName();
    }
}
