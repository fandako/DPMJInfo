package com.example.dpmjinfo;

import android.content.Context;
import android.content.Intent;
import android.media.ImageReader;
import android.os.Bundle;
import android.view.View;

import com.example.dpmjinfo.activities.Departures;

import java.util.List;

public class LineDetailQuery extends ScheduleQuery {
    private LineDetailQueryModel model;

    LineDetailQuery(Context context) {
        super(context);
        model = new LineDetailQueryModel();
    }

    LineDetailQuery(Context context, LineDetailQueryModel model) {
        super(context);
        this.model = model;
    }

    public int getLineId() {
        return model.getLineId();
    }

    public void setLineId(int lineId) {
        model.setLineId(lineId);
    }

    public int getConnectionId(){
        return model.getConnectionId();
    }

    public void setConnectionId(int connectionId) {
        model.setConnectionId(connectionId);
    }

    @Override
    public List<BusStopDeparture> getHighlighted() {
        return model.getHighlighted();
    }

    public void addHighlighted(BusStopDeparture highlighted) {
        model.addHighlighted(highlighted);
    }

    @Override
    public boolean hasHighlighted() {
        return getHighlighted() != null;
    }

    @Override
    protected View getQueryView() {
        //lineDetailQuery does not have any view
        return null;
    }

    @Override
    public void populateView() {
        //lineDetailQuery does not have any view
    }

    @Override
    public String getName() {
        return "Detail spoje";
    }

    @Override
    protected void initView(View v) {
        //lineDetailQuery does not have any view
    }

    @Override
    public List<BusStopDeparture> exec(int page) {
        OfflineFilesManager ofm = new OfflineFilesManager(mContext);
        CISSqliteHelper helper = new CISSqliteHelper(ofm.getFilePath(OfflineFilesManager.SCHEDULE));

        return helper.getLineDepartures(getLineId(), getConnectionId());
    }

    @Override
    public void execAndDisplayResult() {
        Bundle bundle = new Bundle();
        Intent intent;

        intent = new Intent(mContext.getApplicationContext(), Departures.class);
        bundle.putSerializable("com.android.dpmjinfo.queryModel", model);
        bundle.putSerializable("com.android.dpmjinfo.queryClass", this.getClass().getSimpleName());
        intent.putExtras(bundle);

        //start given activity
        mContext.startActivity(intent);
    }
}
