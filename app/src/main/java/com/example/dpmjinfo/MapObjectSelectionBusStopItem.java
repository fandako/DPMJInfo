package com.example.dpmjinfo;

import com.example.dpmjinfo.activities.BusStopDetailActivity;

public class MapObjectSelectionBusStopItem extends MapObjectSelectionItem {

    private BusStop busStop;

    public MapObjectSelectionBusStopItem(BusStop b){
        busStop = b;
    }

    @Override
    public Integer getIconDrawableID() {
        return R.mipmap.bus_stop;
    }

    @Override
    public String getTitle() {
        return busStop.getName();
    }

    @Override
    public Class<?> getDetailActivityClass() {
        return BusStopDetailActivity.class;
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
