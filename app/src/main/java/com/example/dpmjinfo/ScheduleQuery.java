package com.example.dpmjinfo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class ScheduleQuery {
    LayoutInflater mInflater;
    protected Context mContext;
    View mView = null;

    public ScheduleQuery(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    protected abstract View getQueryView();

    public abstract void populateView();

    public View getView() {
        if (mView == null) {
            mView = getQueryView();
            hide();
            initView(mView);
        }

        return mView;
    }

    public void hide() {
        getView().setVisibility(View.GONE);
    }

    public void show() {
        getView().setVisibility(View.VISIBLE);
    }

    public abstract String getName();

    protected abstract void initView(View v);

    public abstract List exec(int page);

    public abstract void execAndDisplayResult();

    public ArrayList<String> getRequiredFileTypes(){
        return new ArrayList<>();
    }

    public boolean isPaginable() {
        return false;
    }

    public boolean hasClickableItems() {
        return false;
    }

    public boolean isAsync() {
        return false;
    }

    public boolean hasHighlighted() {
        return false;
    }

    public BusStopDeparture getHighlighted() {
        return null;
    }

    public RecycleViewClickListener getOnItemTouchListener(Context context, RecyclerView.Adapter adapter) {
        return new DummyOnItemTouchListener();
    }

    private class DummyOnItemTouchListener implements RecycleViewClickListener {
        @Override
        public void onClick(View view, final int position) {
            //do nothing
        }

        @Override
        public void onLongClick(View view, int position) {
            //do nothing
        }
    }

    public static ScheduleQuery getQueryFromSerializedModel(Context context, String className, Serializable model){
        switch (className){
            case "DepartureQuery":
                return new DepartureQuery(context, (DepartureQueryModel) model);
            case "ActualDepartureQuery":
                return new ActualDepartureQuery(context, (ActualDepartureQueryModel) model);
            case "LineDetailQuery":
                return new LineDetailQuery(context, (LineDetailQueryModel) model);
            default: return null;
        }
    }
}
