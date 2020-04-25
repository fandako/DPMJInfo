package com.example.dpmjinfo;

import android.util.Log;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EsriBusStopLoader {
    public static void loadBusStops(EsriBusStopsDoneLoadingListener listener){
        ServiceFeatureTable busStopService = new ServiceFeatureTable("https://gis.jihlava-city.cz/server/rest/services/ost/Ji_MHD_aktualni/MapServer/3");

        QueryParameters query = new QueryParameters();
        // make search case insensitive
        query.setWhereClause("objectid IS NOT NULL AND elp_id IS NOT NULL");

        final ListenableFuture<FeatureQueryResult> future = busStopService.queryFeaturesAsync(query,  ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
        // add done loading listener to fire when the selection returns
        future.addDoneListener(() -> {
            try {
                // call get on the future to get the result
                FeatureQueryResult result = future.get();
                // check there are some results
                Iterator<Feature> resultIterator = result.iterator();
                if(!resultIterator.hasNext()) {
                    Log.d("dbg","No features found");
                }

                List<BusStop> busStops = new ArrayList<BusStop>();

                while (resultIterator.hasNext()) {
                    // get the extent of the first feature in the result to zoom to
                    Feature feature = resultIterator.next();
                    busStops.add(new BusStop(feature));
                }

                listener.esriBusStopsDoneLoading(busStops);
            } catch (Exception e) {
                String error = "Feature search failed for: " + ". Error: " + e.getMessage();
                Log.d("dbg",error);
            }
        });
    }
}
