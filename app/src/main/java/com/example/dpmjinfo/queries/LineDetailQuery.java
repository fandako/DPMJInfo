package com.example.dpmjinfo.queries;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.dpmjinfo.BusStopDeparture;
import com.example.dpmjinfo.helpers.CISSqliteHelper;
import com.example.dpmjinfo.queryModels.LineDetailQueryModel;
import com.example.dpmjinfo.helpers.OfflineFilesManager;
import com.example.dpmjinfo.R;
import com.example.dpmjinfo.activities.DeparturesActivity;

import java.util.List;

/**
 * query object for querying departures of given line connection
 */
public class LineDetailQuery extends ScheduleQuery {
    //private LineDetailQueryModel model;

    public LineDetailQuery(Context context) {
        super(context);
        model = new LineDetailQueryModel();
    }

    public LineDetailQuery(Context context, LineDetailQueryModel model) {
        super(context);
        this.model = model;
    }

    public LineDetailQueryModel getModel() {
        return (LineDetailQueryModel) model;
    }

    public int getLineId() {
        return getModel().getLineId();
    }

    public void setLineId(int lineId) {
        getModel().setLineId(lineId);
    }

    public int getConnectionId(){
        return getModel().getConnectionId();
    }

    public void setConnectionId(int connectionId) {
        getModel().setConnectionId(connectionId);
    }

    @Override
    public List<BusStopDeparture> getHighlighted() {
        return getModel().getHighlighted();
    }

    public void addHighlighted(BusStopDeparture highlighted) {
        getModel().addHighlighted(highlighted);
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
        return mContext.getString(R.string.connection_detail_query_title);
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

        intent = new Intent(mContext.getApplicationContext(), DeparturesActivity.class);
        bundle.putSerializable("com.android.dpmjinfo.queryModel", model);
        bundle.putSerializable("com.android.dpmjinfo.queryClass", this.getClass().getSimpleName());
        intent.putExtras(bundle);

        //start given activity
        mContext.startActivity(intent);
    }
}
