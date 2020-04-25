package com.example.dpmjinfo;

import com.example.dpmjinfo.activities.VehicleDetailActivity;

public class MapObjectSelectionVehicleItem extends MapObjectSelectionItem {
    private Vehicle vehicle;

    public MapObjectSelectionVehicleItem(Vehicle v) {
        vehicle = v;
    }

    @Override
    public Integer getIconDrawableID() {
        return R.drawable.bus_icon;
    }

    @Override
    public String getTitle() {
        return vehicle.getLine() + " " + vehicle.getTerminalStop();
    }

    @Override
    public Class<?> getDetailActivityClass() {
        return VehicleDetailActivity.class;
    }

    @Override
    public String getBundleID() {
        return "com.android.dpmjinfo.vehicle";
    }

    @Override
    public Object getObject() {
        return vehicle;
    }

    @Override
    public Class<?> getObjectClass() {
        return Vehicle.class;
    }
}
