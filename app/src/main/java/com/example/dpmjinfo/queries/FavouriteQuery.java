package com.example.dpmjinfo.queries;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.dpmjinfo.R;
import com.example.dpmjinfo.activities.DeparturesActivity;
import com.example.dpmjinfo.helpers.NetworkHelper;
import com.example.dpmjinfo.helpers.OfflineFileDb;
import com.example.dpmjinfo.queryModels.ScheduleQueryModel;
import com.example.dpmjinfo.recyclerViewHandling.BaseAdapter;
import com.example.dpmjinfo.recyclerViewHandling.FavouriteAdapter;
import com.example.dpmjinfo.recyclerViewHandling.RecycleViewClickListener;

import org.apache.commons.lang3.SerializationUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * query object for querying favourite queries
 */
public class FavouriteQuery extends ScheduleQuery {

    public FavouriteQuery(){}

    public FavouriteQuery(Context context) {
        super(context);
        model = new ScheduleQueryModel();
        model.setShowAddToFavourite(false);
    }

    public FavouriteQuery(Context context, ScheduleQueryModel model){
        super(context);
        this.model = model;
    }

    @Override
    protected View getQueryView() {
        return null;
    }

    @Override
    protected void populateView() {

    }

    @Override
    public String getName() {
        return "Oblíbené";
    }

    @Override
    protected void initView(View v) {

    }

    @Override
    public boolean hasClickableItems() {
        return true;
    }

    @Override
    public Class getObjectClass() {
        return ScheduleQuery.class;
    }

    @Override
    public BaseAdapter getAdapter() {
        return new FavouriteAdapter();
    }

    @Override
    public List<ScheduleQuery> exec(int page) {
        OfflineFileDb db = new OfflineFileDb(mContext);

        List<Pair<String, ScheduleQueryModel>> favourites = db.getFavourites();

        List<ScheduleQuery> result = new ArrayList<>();

        for (Pair<String, ScheduleQueryModel> p: favourites) {
            result.add(ScheduleQuery.getQueryFromSerializedModel(mContext, p.first, p.second));
        }

        return result;
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

    @Override
    public RecycleViewClickListener getOnItemTouchListener(Context context, RecyclerView.Adapter adapter) {
        return new OnFavouriteTouchListener(context, (FavouriteAdapter) adapter);
    }

    private class OnFavouriteTouchListener implements RecycleViewClickListener {

        Context context;
        FavouriteAdapter adapter;

        OnFavouriteTouchListener(Context context, FavouriteAdapter adapter) {
            this.context = context;
            this.adapter = adapter;
        }


        private void openDetail(int position) {
            ScheduleQuery query = adapter.getItem(position);

            ScheduleQuery tempQuery = ScheduleQuery.getQueryFromSerializedModel(context, query.getClass().getSimpleName(), SerializationUtils.clone(query.getModel()));


            //init values that change with time like for example date for Departure and Connection queries
            tempQuery.intiForFavourite();

            if(tempQuery.isInternetDependant()){
                if(!NetworkHelper.isNetworkAvailable(mContext)){
                    Toast.makeText(mContext, mContext.getString(R.string.no_internet_connection_alert_message), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Bundle bundle = new Bundle();
            Intent intent;

            intent = new Intent(mContext.getApplicationContext(), DeparturesActivity.class);
            bundle.putSerializable("com.android.dpmjinfo.queryModel", tempQuery.getModel());
            bundle.putSerializable("com.android.dpmjinfo.queryClass", tempQuery.getClass().getSimpleName());
            intent.putExtras(bundle);

            //start given activity
            mContext.startActivity(intent);
        }

        @Override
        public void onClick(View view, final int position) {
            openDetail(position);
        }

        @Override
        public void onLongClick(View view, int position) {
            openDetail(position);
        }
    }
}
