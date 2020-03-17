package com.example.dpmjinfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArraySet;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.data.TileCache;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.io.RequestConfiguration;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatusChangedEvent;
import com.esri.arcgisruntime.loadable.LoadStatusChangedListener;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.LayerViewStateChangedEvent;
import com.esri.arcgisruntime.mapping.view.LayerViewStateChangedListener;
import com.esri.arcgisruntime.mapping.view.LayerViewStatus;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.TextSymbol;
import com.example.dpmjinfo.debug.BusStopDetail;
import com.example.dpmjinfo.debug.MapKey;
import com.example.dpmjinfo.debug.MapObjectSelection;
import com.example.dpmjinfo.debug.VehicleDetail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private ArcGISMap map;
    private MapView mMapView;
    private static LayerList mOperationalLayers;
    private MobileMapPackage mapPackage;

    private int mInterval = 5*1000; // 60 seconds by default, can be changed later
    private Handler mHandler;
    private BroadcastReceiver _broadcastReceiver;
    private boolean updateVehicles;
    private ServiceFeatureTable serviceFeatureTable2;
    private GraphicsOverlay vehiclesOverlay;
    private GraphicsOverlay vehicleInfoOverlay;

    DownloadManager downloadManager;
    private long downloadID;
    private String mapPath = "";
    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                //Toast.makeText(MainActivity.this, "Download Completed", Toast.LENGTH_SHORT).show();
                Log.d("dbg", "download complete");
                //loadMobileMapPackage(mapPath);
                setupMap();
            }
        }
    };

    private static final int SCALE = 40000;

    File file;

    private class IdentifyFeatureLayerTouchListener extends DefaultMapViewOnTouchListener {

        private FeatureLayer layer = null; // reference to the layer to identify features in
        private GraphicsOverlay graphicsOverlay = null; //reference to the graphics overlay to identify vehicles in
        private ArraySet<BusStop> identifiedBusStops = new ArraySet<>();

        // provide a default constructor
        public IdentifyFeatureLayerTouchListener(Context context, MapView mapView, FeatureLayer layerToIdentify, GraphicsOverlay graphicsOverlayToIdentify) {
            super(context, mapView);
            layer = layerToIdentify;
            graphicsOverlay = graphicsOverlayToIdentify;
        }

        // override the onSingleTapConfirmed gesture to handle a single tap on the MapView
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // get the screen point where user tapped
            android.graphics.Point screenPoint = new android.graphics.Point((int) e.getX(), (int) e.getY());
            // ...
            // call identifyLayerAsync, specifying the layer to identify, the screen location, tolerance, types to return, and
            // maximum results
            final ListenableFuture<IdentifyLayerResult> identifyFuture =
                    mMapView.identifyLayerAsync(layer, screenPoint, 20, false, 25);

            // add a listener to the future
            identifyFuture.addDoneListener(new Runnable() {

                @Override
                public void run() {
                    try {
                        // get the identify results from the future - returns when the operation is complete
                        IdentifyLayerResult identifyLayerResult = identifyFuture.get();
                        // ...
                        // a reference to the feature layer can be used, for example, to select identified features
                        FeatureLayer featureLayer = null;
                        if (identifyLayerResult.getLayerContent() instanceof FeatureLayer) {
                            featureLayer = (FeatureLayer) identifyLayerResult.getLayerContent();
                        }

                        //remove bus stops identified on previous touch event
                        identifiedBusStops.clear();

                        // iterate each identified geoelement from the specified layer and cast to Feature
                        for (GeoElement identifiedElement : identifyLayerResult.getElements()) {
                            if (identifiedElement instanceof Feature) {
                                // access attributes or geometry of the feature, or select it as shown below
                                Feature identifiedFeature = (Feature) identifiedElement;
                                BusStop busStop = new BusStop(identifiedFeature);
                                //Log.d("dbg", "zastavka: " + busStop.getName() + "; " + busStop.getElp_id());

                                //do not include stops without elp_id
                                if(busStop.getElp_id() != null)
                                    identifiedBusStops.add(busStop);
                            }
                        }

                        final ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphic = mMapView.identifyGraphicsOverlayAsync(graphicsOverlay, screenPoint, 10.0, false, 2);

                        identifyGraphic.addDoneListener(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    IdentifyGraphicsOverlayResult grOverlayResult = identifyGraphic.get();

                                    // get the list of graphics returned by identify graphic overlay
                                    List<Graphic> graphics = grOverlayResult.getGraphics();

                                    //if no objects were found return
                                    if(graphics.isEmpty() && identifiedBusStops.isEmpty()){
                                        return;
                                    }

                                    Bundle bundle = new Bundle();
                                    Intent intent;

                                    //if single object selected -> display detail, otherwise display object selection activity
                                    if(graphics.size() + identifiedBusStops.size() == 1){
                                        if(identifiedBusStops.isEmpty()){
                                            intent = new Intent(getApplicationContext(), VehicleDetail.class);
                                            bundle.putSerializable("com.android.dpmjinfo.vehicle", new Vehicle(graphics.get(0).getAttributes()));
                                            intent.putExtras(bundle);
                                        }else{
                                            intent = new Intent(getApplicationContext(), BusStopDetail.class);
                                            bundle.putSerializable("com.android.dpmjinfo.busStop", identifiedBusStops.valueAt(0));
                                            intent.putExtras(bundle);
                                        }
                                    }else{
                                        //convert graphics to serializable array of vehicle
                                        ArrayList<Vehicle> vehicles = new ArrayList<>();
                                        for (Graphic graphic : graphics) {
                                            vehicles.add(new Vehicle(graphic.getAttributes()));
                                        }

                                        intent = new Intent(getApplicationContext(), MapObjectSelection.class);
                                        bundle.putSerializable("com.android.dpmjinfo.busStops", new ArrayList<BusStop>(identifiedBusStops));
                                        bundle.putSerializable("com.android.dpmjinfo.vehicles", vehicles);
                                        intent.putExtras(bundle);
                                    }

                                    //start given activity
                                    startActivity(intent);
                                }catch(InterruptedException | ExecutionException ie){
                                    ie.printStackTrace();
                                }

                            }
                        });
                    } catch (InterruptedException | ExecutionException ex) {
                        // must deal with checked exceptions thrown from the async identify operation
                        Log.e("exc", ex.getMessage());
                    }
                }
            });

            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton infoButton = findViewById(R.id.mapKey);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapKey.class);
                startActivity(intent);
            }
        });

        updateVehicles = false;

        mHandler = new Handler();

        //update vehicle position and info on minute change aka action_time_tick
        _broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0){
                    mHandler.postDelayed(mStatusChecker, mInterval);
                }
            }
        };
        registerReceiver(_broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

        mMapView = findViewById(R.id.mapView);

        /*mMapView.addLayerViewStateChangedListener(new LayerViewStateChangedListener() {
            @Override
            public void layerViewStateChanged(LayerViewStateChangedEvent layerViewStateChangedEvent) {
                // Each layer may have more than one layer view state.
                StringBuilder layerStatuses = new StringBuilder();
                for (LayerViewStatus status : layerViewStateChangedEvent.getLayerViewStatus()) {
                    if (layerStatuses.length() > 0) {
                        layerStatuses.append(",");
                    }
                    layerStatuses.append(status.name());
                }

                Log.d("dbg","Layer " + layerViewStateChangedEvent.getLayer().getName() + " status=" + layerStatuses.toString());
            }
        });*/

        registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        downloadBasemap();
    }

    private void downloadBasemap(){
        mapPath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/test1.tpk";
        Log.d("dbg", mapPath);

        file = new File(mapPath);

        //check if map is present in device
        if(file.exists()){
            //Log.d("dbg", "map already exists");
            setupMap();
            //loadMobileMapPackage(mapPath);
            return;
        }

        downloadManager= (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        try{
       /*
       Create a DownloadManager.Request with all the information necessary to start the download
        */
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://10.0.0.139/test1.tpk"));
        //Restrict the types of networks over which this download may proceed.
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        //Set whether this download may proceed over a roaming connection.
        request.setAllowedOverRoaming(false);
        //Set the title of this download, to be displayed in notifications (if enabled).
        request.setTitle("test1.tpk");
        //Set a description of this download, to be displayed in notifications (if enabled)
        request.setDescription("Downloding...");
        request.setMimeType("application/octet-stream");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setDestinationUri(Uri.fromFile(file));// Uri of the destination file
        request.allowScanningByMediaScanner();

        Log.d("dbg", "enqueue download");
        downloadID = downloadManager.enqueue(request);// enqueue puts the download request in the queue.
            }catch (IllegalArgumentException e) {
            //BaseUtils.showToast(mContext, "Download link is broken or not availale for download");
            Log.e("error", "Line no: 455,Method: downloadFile: Download link is broken");

        }


    }

    /**
     * Load a mobile map package into a MapView
     *
     * @param mmpkFile Full path to mmpk file
     */
    /*private void loadMobileMapPackage(String mmpkFile) {
        //[DocRef: Name=Open Mobile Map Package-android, Category=Work with maps, Topic=Create an offline map]
        // create the mobile map package
        mapPackage = new MobileMapPackage(mmpkFile);
        // load the mobile map package asynchronously
        mapPackage.loadAsync();

        // add done listener which will invoke when mobile map package has loaded
        mapPackage.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                // check load status and that the mobile map package has maps
                if (mapPackage.getLoadStatus() == LoadStatus.LOADED && !mapPackage.getMaps().isEmpty()) {
                    // add the map from the mobile map package to the MapView
                    mMapView.setMap(mapPackage.getMaps().get(0));
                    setupMap();
                } else {
                    // log an issue if the mobile map package fails to load
                    Log.e("dbg", mapPackage.getLoadError().getMessage());
                }
            }
        });
        //[DocRef: END]
    }*/

    private void setupMap() {
        if (mMapView != null) {
            /*ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 49.400433, -15.588519, 10);

            ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable("https://gis.jihlava-city.cz/server/rest/services/ost/Ji_MHD_aktualni/MapServer/5");

            // create the feature layer using the service feature table
            FeatureLayer featureLayer = new FeatureLayer(serviceFeatureTable);

            // add the layer to the map
            map.getOperationalLayers().add(featureLayer);*/

            //setup online basemap
            /*"df3053acfc454cbba4c041c27d40c7f0"*/
            /*String itemId =  "de45809a789d42219be78f9d96a7e217";
            Portal portal = new Portal("https://www.arcgis.com", false);
            PortalItem portalItem = new PortalItem(portal, itemId);
            map = new ArcGISMap(portalItem);*/
            Log.d("dbg", "setting up map: " + mapPath);
            Log.d("dbg", Uri.fromFile(file).getPath());
            //Log.d("dbg", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/test1.tpk");
            // instantiate a vector tiled layer with the path to the vtpk file
            /*VectorTileCache cache = new VectorTileCache(mapPath);
            ArcGISVectorTiledLayer localVectorTiledLayer = new ArcGISVectorTiledLayer(cache);
            localVectorTiledLayer.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    if(localVectorTiledLayer.getLoadStatus() == LoadStatus.LOADED){

                    }else {
                        Log.d("dbg", localVectorTiledLayer.getLoadError().getMessage());
                    }
                }
            });
            localVectorTiledLayer.addLoadStatusChangedListener(new LoadStatusChangedListener() {
                @Override
                public void loadStatusChanged(LoadStatusChangedEvent loadStatusChangedEvent) {
                    Log.d("dbg", loadStatusChangedEvent.getNewLoadStatus().name());
                }
            });
            map = new ArcGISMap(new Basemap(localVectorTiledLayer));*/
            // instantiate a tiledLayer with the path to the tpk file and add it to a map
            TileCache cache = new TileCache(Uri.fromFile(file).getPath());
            ArcGISTiledLayer localTiledLayer = new ArcGISTiledLayer(cache);
            localTiledLayer.addLoadStatusChangedListener(new LoadStatusChangedListener() {
                @Override
                public void loadStatusChanged(LoadStatusChangedEvent loadStatusChangedEvent) {
                    Log.d("dbg tiled status change", loadStatusChangedEvent.getNewLoadStatus().name());
                }
            });
            localTiledLayer.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    //Log.d("dbg tiled done loading", localTiledLayer.getLoadError().getMessage() + ", " + localTiledLayer.getLoadError().getAdditionalMessage());
                }
            });
            map = new ArcGISMap(new Basemap(localTiledLayer));

            // create overlay to contain vehicle graphic
            vehiclesOverlay = new GraphicsOverlay();
            // add to map view
            mMapView.getGraphicsOverlays().add(vehiclesOverlay);

            //create overlay containing vehicle info
            vehicleInfoOverlay = new GraphicsOverlay();
            // add to map view
            mMapView.getGraphicsOverlays().add(vehicleInfoOverlay);

            ServiceFeatureTable serviceFeatureTable6 = new ServiceFeatureTable("https://gis.jihlava-city.cz/server/rest/services/ost/Ji_MHD_aktualni/MapServer/1");
            FeatureLayer vehiclesOnLine2 = new FeatureLayer(serviceFeatureTable6);

            serviceFeatureTable2 = new ServiceFeatureTable("https://gis.jihlava-city.cz/server/rest/services/ost/Ji_MHD_aktualni/MapServer/6");
            // create the feature layer using the service feature table
            /*serviceFeatureTable2.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE);
            QueryParameters query = new QueryParameters();
            // make search case insensitive
            query.setWhereClause("objectid IS NOT NULL");
            List<String> list = new ArrayList();
            list.add("*");
            serviceFeatureTable2.populateFromServiceAsync(query, true, list);*/

            FeatureLayer vehiclesOnLine = new FeatureLayer(serviceFeatureTable2);
            vehiclesOnLine.setVisible(true);

            serviceFeatureTable2.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    startRepeatingTask();

                    /*for (Field field:serviceFeatureTable2.getFields()
                         ) {
                        Log.d("dbg", "field: " + field.getName() + ", type: " + field.getFieldType());
                    }*/

                    /*// create objects required to do a selection with a query
                    QueryParameters query = new QueryParameters();
                    // make search case insensitive
                    query.setWhereClause("objectid IS NOT NULL");
                    // call select features

                    Log.d("dbg","feature total count: " + serviceFeatureTable2.getTotalFeatureCount());
                    final ListenableFuture<FeatureQueryResult> future = serviceFeatureTable2.queryFeaturesAsync(query, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);

                    // add done loading listener to fire when the selection returns
                    future.addDoneListener(() -> {
                        try {
                            Integer cnt = 0;
                            // call get on the future to get the result
                            FeatureQueryResult result = future.get();
                            // check there are some results
                            Iterator<Feature> resultIterator = result.iterator();
                            if(!resultIterator.hasNext()) {
                                Log.d("dbg","No features found");
                            }
                            List<Graphic> graphics = new ArrayList<Graphic>();
                            while (resultIterator.hasNext()) {
                                // get the extent of the first feature in the result to zoom to
                                Feature feature = resultIterator.next();

                                Point p = new Point((double) feature.getAttributes().get("longitude"), (double) feature.getAttributes().get("latitude"), SpatialReferences.getWgs84());
                                SimpleMarkerSymbol redCircleSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF0000, 10);
                                Graphic vehicle = new Graphic(p, redCircleSymbol);
                                graphics.add(vehicle);
                                //Feature f = vehiclesOnLine2.getFeatureTable().createFeature(feature.getAttributes(), p);

                                //feature.getAttributes().forEach((k, v) -> Log.d("dbg", k + ": " + v));
                                for (Map.Entry<String, Object> entry : feature.getAttributes().entrySet()) {
                                    Log.d("dbg", entry.getKey() + ": " + entry.getValue());
                                }
                                //Log.d("dbg","vehicle: " + feature.getAttributes().get("linka") + " konecna: " + feature.getAttributes().get("konecna"));
                                cnt++;
                            }
                            graphicsOverlay.getGraphics().addAll(graphics);
                            Log.d("dbg","vehicles cnt from query: " + cnt);
                        } catch (Exception e) {
                            String error = "Vehicle search failed for: " + ". Error: " + e.getMessage();
                            Log.d("dbg",error);
                        }

                    });*/
                }
            });

            /*SimpleMarkerSymbol sms = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE ,Color.BLUE, 50);
            // instantiate a simple renderer with the symbol created above
            SimpleRenderer simRenderer = new SimpleRenderer(sms);
            vehiclesOnLine.setRenderer(simRenderer);*/

            map.getOperationalLayers().add(vehiclesOnLine);

            ServiceFeatureTable serviceFeatureTable4 = new ServiceFeatureTable("https://gis.jihlava-city.cz/server/rest/services/ost/Ji_MHD_aktualni/MapServer/2");
            // create the feature layer using the service feature table
            serviceFeatureTable4.setRequestConfiguration(new RequestConfiguration());
            FeatureLayer vehiclesWaiting = new FeatureLayer(serviceFeatureTable4);

            map.getOperationalLayers().add(vehiclesWaiting);

            ServiceFeatureTable serviceFeatureTable3 = new ServiceFeatureTable("https://gis.jihlava-city.cz/server/rest/services/ost/Ji_MHD_aktualni/MapServer/3");
            // create the feature layer using the service feature table
            FeatureLayer stops = new FeatureLayer(serviceFeatureTable3);

            /*serviceFeatureTable3.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    // create objects required to do a selection with a query
                    QueryParameters query = new QueryParameters();
                    // make search case insensitive
                    query.setWhereClause("objectid IS NOT NULL");
                    // call select features
                    Log.d("dbg","feature total count: " + serviceFeatureTable3.getTotalFeatureCount());
                    final ListenableFuture<FeatureQueryResult> future = serviceFeatureTable3.queryFeaturesAsync(query,  ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
                    // add done loading listener to fire when the selection returns
                    future.addDoneListener(() -> {
                        try {
                            Integer cnt = 0;
                            // call get on the future to get the result
                            FeatureQueryResult result = future.get();
                            // check there are some results
                            Iterator<Feature> resultIterator = result.iterator();
                            if(!resultIterator.hasNext()) {
                                Log.d("dbg","No features found");
                            }
                            while (resultIterator.hasNext()) {
                                // get the extent of the first feature in the result to zoom to
                                Feature feature = resultIterator.next();
                                Log.d("dbg","stop: " + feature.getAttributes().get("nazev") + " linky: " + feature.getAttributes().get("linky"));
                                cnt++;
                            }
                            Log.d("dbg","stops cnt from query: " + cnt);
                        } catch (Exception e) {
                            String error = "Feature search failed for: " + ". Error: " + e.getMessage();
                            Log.d("dbg",error);
                        }

                    });
                }
            });*/

            // add the layer to the map
            map.getOperationalLayers().add(stops);
            mMapView.setOnTouchListener(new IdentifyFeatureLayerTouchListener(this, mMapView, stops, vehiclesOverlay));

            ServiceFeatureTable serviceFeatureTable5 = new ServiceFeatureTable("https://gis.jihlava-city.cz/server/rest/services/ost/Ji_MHD_aktualni/MapServer/4");
            // create the feature layer using the service feature table
            FeatureLayer linesMap = new FeatureLayer(serviceFeatureTable5);

            linesMap.setVisible(true);

            map.getOperationalLayers().add(linesMap);

            ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable("https://gis.jihlava-city.cz/server/rest/services/ost/Ji_MHD_aktualni/MapServer/5");
            // create the feature layer using the service feature table
            serviceFeatureTable.setRequestConfiguration(new RequestConfiguration());

            FeatureLayer lines = new FeatureLayer(serviceFeatureTable);
            lines.setVisible(false);
            /*serviceFeatureTable.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    // create objects required to do a selection with a query
                    QueryParameters query = new QueryParameters();
                    // make search case insensitive
                    query.setWhereClause("linka = 'Linka 3' OR linka = 'Linka 12' OR linka = 'Linka 5'");
                    // call select features
                    Log.d("dbg","feature total count: " + serviceFeatureTable.getTotalFeatureCount());
                    final ListenableFuture<FeatureQueryResult> future = serviceFeatureTable.queryFeaturesAsync(query,  ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
                    // add done loading listener to fire when the selection returns
                    future.addDoneListener(() -> {
                        try {
                            Integer cnt = 0;
                            // call get on the future to get the result
                            FeatureQueryResult result = future.get();
                            // check there are some results
                            Iterator<Feature> resultIterator = result.iterator();
                            if(!resultIterator.hasNext()) {
                                Log.d("dbg","No features found");
                            }
                            while (resultIterator.hasNext()) {
                                // get the extent of the first feature in the result to zoom to
                                Feature feature = resultIterator.next();
                                lines.setFeatureVisible(feature, false);
                                Log.d("dbg","feature: " + feature.getAttributes().get("linka"));
                                cnt++;
                            }
                            Log.d("dbg","Feature cnt from query: " + cnt);
                        } catch (Exception e) {
                            String error = "Feature search failed for: " + ". Error: " + e.getMessage();
                            Log.d("dbg",error);
                        }

                    });
                }
            });*/

            /*lines.getFeatureTable().addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    Log.d("dbg","fields count " + lines.getFeatureTable().getFields().size());

                    List<Field> fields = lines.getFeatureTable().getFields();

                    for (Field field:fields
                    ) {
                        Log.d("dbg","field " + field.getName());
                    }

                    Log.d("dbg","features count " + lines.getFeatureTable().getTotalFeatureCount());
                }
            });*/


            // add the layer to the map
            map.getOperationalLayers().add(lines);

            mOperationalLayers = map.getOperationalLayers();

            mMapView.setMap(map);
            Point startPoint = new Point(15.591035, 49.395911, SpatialReferences.getWgs84());

            //set viewpoint of map view to starting point and scaled
            mMapView.setViewpointCenterAsync(startPoint, SCALE);
        }
    }

    public String getURLForResource (int resourceId) {
        //use BuildConfig.APPLICATION_ID instead of R.class.getPackage().getName() if both are not same
        return Uri.parse("android.resource://"+R.class.getPackage().getName()+"/" +resourceId).toString();
    }

    void updateStatus(){
        // create objects required to do a selection with a query
        QueryParameters query = new QueryParameters();
        // make search case insensitive
        query.setWhereClause("objectid IS NOT NULL");
        // call select features

        Log.d("dbg","feature total count: " + serviceFeatureTable2.getTotalFeatureCount());
        final ListenableFuture<FeatureQueryResult> future = serviceFeatureTable2.queryFeaturesAsync(query, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);

        // add done loading listener to fire when the selection returns
        future.addDoneListener(() -> {
            try {
                Integer cnt = 0;
                // call get on the future to get the result
                FeatureQueryResult result = future.get();
                // check there are some results
                Iterator<Feature> resultIterator = result.iterator();
                if(!resultIterator.hasNext()) {
                    Log.d("dbg","No features found");
                    return;
                }

                vehiclesOverlay.getGraphics().clear();
                vehicleInfoOverlay.getGraphics().clear();
                List<Graphic> graphics = new ArrayList<Graphic>();
                while (resultIterator.hasNext()) {
                    // get the extent of the first feature in the result to zoom to
                    Feature feature = resultIterator.next();

                    //PictureMarkerSymbol vehicleSymbol;
                    ListenableFuture<PictureMarkerSymbol> vehicleSymbolFuture;
                    if(feature.getAttributes().get("typ") == "autobus") {
                        //bus
                        /*vehicleSymbol = new PictureMarkerSymbol("http://10.0.0.139/bus.png")*/;
                        vehicleSymbolFuture = PictureMarkerSymbol.createAsync((BitmapDrawable)getDrawable(R.drawable.bus));
                    } else {
                        //trolleybus
                        /*vehicleSymbol = new PictureMarkerSymbol("http://10.0.0.139/trolleybus.png");*/
                        vehicleSymbolFuture = PictureMarkerSymbol.createAsync((BitmapDrawable)getDrawable(R.drawable.trolleybus));
                    }
                    vehicleSymbolFuture.addDoneListener(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                PictureMarkerSymbol vehicleSymbol = vehicleSymbolFuture.get();
                                vehicleSymbol.setHeight(48);
                                vehicleSymbol.setWidth(48);

                                if (feature.getAttributes().get("azimut") != null) {
                                    vehicleSymbol.setAngle(Float.parseFloat("" + feature.getAttributes().get("azimut")));
                                }

                                Point p = new Point((double) feature.getAttributes().get("longitude"), (double) feature.getAttributes().get("latitude"), SpatialReferences.getWgs84());
                                Graphic vehicle = new Graphic(p, feature.getAttributes(), vehicleSymbol);
                                vehicle.setZIndex(1);
                                graphics.add(vehicle);
                                vehiclesOverlay.getGraphics().add(vehicle);

                                //text symbol which defines text size, the text and color
                                TextSymbol txtSymbol = new TextSymbol(10, (String) feature.getAttributes().get("linka"), Color.BLACK, TextSymbol.HorizontalAlignment.LEFT, TextSymbol.VerticalAlignment.MIDDLE);
                                txtSymbol.setOffsetX(-4);
                                txtSymbol.setFontWeight(TextSymbol.FontWeight.BOLD);
                                //create a graphic from the point and symbol
                                Graphic gr = new Graphic(p, txtSymbol);
                                gr.setZIndex(999);
                                vehicleInfoOverlay.getGraphics().add(gr);

                                Short delay = (Short) feature.getAttributes().get("delayinmins");

                                if(delay != 0) {
                                    TextSymbol delayTxtSymbol = new TextSymbol(10, delay.toString() + " min", Color.RED, TextSymbol.HorizontalAlignment.LEFT, TextSymbol.VerticalAlignment.MIDDLE);
                                    delayTxtSymbol.setOffsetX(20);
                                    delayTxtSymbol.setFontWeight(TextSymbol.FontWeight.BOLD);
                                    //create a graphic from the point and symbol
                                    Graphic gr2 = new Graphic(p, delayTxtSymbol);
                                    gr2.setZIndex(998);
                                    vehicleInfoOverlay.getGraphics().add(gr2);
                                }
                            } catch (Exception e) {
                                String error = "Vehicle symbol failed . Error: " + e.getMessage();
                                Log.d("dbg",error);
                            }
                        }
                    });
                    /*vehicleSymbol.setHeight(48);
                    vehicleSymbol.setWidth(48);

                    if(feature.getAttributes().get("azimut") != null){
                        //Log.d("dbg", "azimut: " + feature.getAttributes().get("azimut"));
                        vehicleSymbol.setAngle(Float.parseFloat("" + feature.getAttributes().get("azimut")));
                    }

                    vehicleSymbol.loadAsync();


                    vehicleSymbol.addDoneLoadingListener(new Runnable() {
                        @Override
                        public void run() {
                            Point p = new Point((double) feature.getAttributes().get("longitude"), (double) feature.getAttributes().get("latitude"), SpatialReferences.getWgs84());
                            Graphic vehicle = new Graphic(p, vehicleSymbol);
                            vehicle.setZIndex(1);
                            graphics.add(vehicle);

                            //text symbol which defines text size, the text and color
                            TextSymbol txtSymbol = new TextSymbol(10, (String) feature.getAttributes().get("linka"),Color.BLACK, TextSymbol.HorizontalAlignment.LEFT, TextSymbol.VerticalAlignment.MIDDLE);
                            txtSymbol.setOffsetX(-4);
                            //create a graphic from the point and symbol
                            Graphic gr = new Graphic(p, txtSymbol);
                            gr.setZIndex(999);

                            //add the graphic to the map
                            graphicsOverlay.getGraphics().add(vehicle);
                            graphicsOverlay.getGraphics().add(gr);
                        }
                    });*/


                    //Feature f = vehiclesOnLine2.getFeatureTable().createFeature(feature.getAttributes(), p);

                    //feature.getAttributes().forEach((k, v) -> Log.d("dbg", k + ": " + v));
                    /*for (Map.Entry<String, Object> entry : feature.getAttributes().entrySet()) {
                        Log.d("dbg", entry.getKey() + ": " + entry.getValue());
                    }*/
                    //Log.d("dbg","vehicle: " + feature.getAttributes().get("linka") + " konecna: " + feature.getAttributes().get("konecna"));
                    cnt++;
                }
                //graphicsOverlay.getGraphics().addAll(graphics);
                Log.d("dbg","vehicles cnt from query: " + cnt);
            } catch (Exception e) {
                String error = "Vehicle search failed for: " + ". Error: " + e.getMessage();
                Log.d("dbg",error);
            }

        });
    }

    private Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if(updateVehicles)
                    updateStatus(); //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                //mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        updateVehicles = true;
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Override
    protected void onPause(){
        mMapView.pause();
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.dispose();
        stopRepeatingTask();
        unregisterReceiver(onDownloadComplete);
        if (_broadcastReceiver != null)
            unregisterReceiver(_broadcastReceiver);
    }
}
