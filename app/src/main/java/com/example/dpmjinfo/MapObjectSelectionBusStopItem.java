package com.example.dpmjinfo;

import android.graphics.drawable.Drawable;

import com.example.dpmjinfo.debug.BusStopDetail;

public class MapObjectSelectionBusStopItem extends MapObjectSelectionItem {

    private BusStop busStop;

    public MapObjectSelectionBusStopItem(BusStop b){
        busStop = b;
    }

    @Override
    public Integer getIconDrawableID() {
        return R.drawable.bus_stop;
    }

    @Override
    public String getTitle() {
        return busStop.getName();
    }

    @Override
    public Class<?> getDetailActivityClass() {
        return BusStopDetail.class;
    }

    @Override
    public String getBundleID() {
        return "com.android.dpmjinfo.busStop";
    }

    @Override
    public Object getObject() {
        return busStop;
    }

    @Override
    public Class<?> getObjectClass() {
        return BusStop.class;
    }
}
