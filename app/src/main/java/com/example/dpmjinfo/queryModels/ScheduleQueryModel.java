package com.example.dpmjinfo.queryModels;

import com.example.dpmjinfo.queries.ScheduleQuery;

import java.io.Serializable;

public class ScheduleQueryModel implements Serializable, Cloneable {
    protected boolean showAddToFavourite;

    public ScheduleQueryModel() {
        showAddToFavourite = true;
    }

    public boolean isShowAddToFavourite() {
        return showAddToFavourite;
    }

    public void setShowAddToFavourite(boolean showAddToFavourite) {
        this.showAddToFavourite = showAddToFavourite;
    }
}
