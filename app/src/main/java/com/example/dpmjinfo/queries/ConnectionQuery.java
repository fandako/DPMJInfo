package com.example.dpmjinfo.queries;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;

import com.example.dpmjinfo.queryModels.ScheduleQueryModel;
import com.example.dpmjinfo.recyclerViewHandling.BaseAdapter;
import com.example.dpmjinfo.BusStop;
import com.example.dpmjinfo.BusStopDeparture;
import com.example.dpmjinfo.helpers.CISSqliteHelper;
import com.example.dpmjinfo.Connection;
import com.example.dpmjinfo.recyclerViewHandling.ConnectionAdapter;
import com.example.dpmjinfo.queryModels.ConnectionQueryModel;
import com.example.dpmjinfo.queryViews.ConnectionQueryView;
import com.example.dpmjinfo.EdgeSequence;
import com.example.dpmjinfo.helpers.OfflineFilesManager;
import com.example.dpmjinfo.R;
import com.example.dpmjinfo.ScheduleGraphEdge;
import com.example.dpmjinfo.ScheduleGraphNode;
import com.example.dpmjinfo.activities.DeparturesActivity;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * query object for querying connections between two stops
 */
public class ConnectionQuery extends ScheduleQuery {
    private CISSqliteHelper mDb = null;
    private List<BusStop> targetStops;
    private List<BusStop> startStops;
    //private ConnectionQueryModel model;
    private List<Connection> foundConnections;
    private HashMap<Integer, HashMap<Integer, LinkedList<ScheduleGraphEdge>>> graphEdges = null;
    private HashMap<Integer, HashMap<Integer, LinkedList<ScheduleGraphEdge>>> footConnectionsEdges = null;

    private BusStop removedTargetStop = null;
    private int removedTargetStopPosition;


    private static final int DEFAULT_PAGE_SIZE = 10;
    //constant to use as line id for foot connections
    public static final int FOOT_CONNECTION_LINE_ID = -2;

    private ConnectionQueryView view;

    public ConnectionQuery(Context context/*, SQLiteDatabase db*/) {
        super(context);
        //mDb = db;
        model = new ConnectionQueryModel();
        //mContext = context;

        initLocalVars();
        initModelValues();
    }

    public ConnectionQuery(Context context, ConnectionQueryModel model) {
        super(context);
        //mContext = context;
        this.model = model;

        initLocalVars();
    }

    public ConnectionQueryModel getModel() {
        return (ConnectionQueryModel) model;
    }

    @Override
    public void intiForFavourite() {
        setDate(DateTime.now().toString(getDateFormat()));
        getModel().setShowAddToFavourite(false);
    }

    @Override
    public List<Pair<String, String>> getSummary() {
        List<Pair<String, String>> summary = new ArrayList<>();

        summary.add(new Pair<>(mContext.getString(R.string.departure_query_time_label), getTime()));
        summary.add(new Pair<>(mContext.getString(R.string.connection_query_start_stop_label), getModel().getStartStop().getName()));
        summary.add(new Pair<>(mContext.getString(R.string.connection_query_target_stop_label), getModel().getTargetStop().getName()));

        return summary;
    }

    private void initLocalVars() {
        targetStops = new ArrayList<>();
        startStops = new ArrayList<>();
        foundConnections = new ArrayList<>();
    }

    private void initModelValues() {
        setDate(getInitialDate());
        setTime(getInitialTime());
        setPageSize(DEFAULT_PAGE_SIZE);
    }

    public int getStartStopId() {
        return getModel().getStartStopId();
    }

    public void setStartStop(BusStop stop) {
        getModel().setStartStop(stop);
    }

    public int getTargetStopId() {
        return getModel().getTargetStopId();
    }

    public void setTargetStop(BusStop stop) {
        getModel().setTargetStop(stop);
    }

    public String getDate() {
        return getModel().getDate();
    }

    public void setDate(String date) {
        getModel().setDate(date);
    }

    public String getTime() {
        return getModel().getTime();
    }

    public void setTime(String time) {
        getModel().setTime(time);
    }

    @Override
    public int getPageSize() {
        return getModel().getPageSize();
    }

    public void setPageSize(int pageSize) {
        getModel().setPageSize(pageSize);
    }

    @Override
    public boolean isPaginable() {
        return true;
    }

    @Override
    public boolean hasClickableItems() {
        return false;
    }

    protected View getQueryView() {
        //return mInflater.inflate(R.layout.departure_query, null, false);
        if (view == null) {
            view = new ConnectionQueryView(this, mContext);
        }

        return view;
    }

    private CISSqliteHelper getDb() {
        if (mDb == null) {
            OfflineFilesManager ofm = new OfflineFilesManager(mContext);
            mDb = new CISSqliteHelper(ofm.getFilePath(OfflineFilesManager.SCHEDULE));
        }

        return mDb;
    }

    @Override
    protected void populateView() {
        List<BusStop> busStops = getDb().getBusStops();
        //important to call before start stop initialization as onStartStopSelected is dependant on targetStop value
        targetStops.addAll(busStops);
        notifyTargetStopsChanged();


        startStops.addAll(busStops);
        notifyStartStopsChanged();
    }

    public String getName() {
        return mContext.getString(R.string.connection_query_title);
    }

    protected void initView(View v) {

    }

    @Override
    public boolean isAsync() {
        return true;
    }

    /**
     * performs dijkstra algorithm on departure graph to retrieve shortest path for given time
     * @param graphEdges departure defined edges
     * @param footConnectionsEdges foot connection defined edges
     * @param startStop start stop id
     * @param targetStop target stop id
     * @param time time
     * @return shortest path as list of departures
     */
    private List<BusStopDeparture> Dijkstra(HashMap<Integer, HashMap<Integer, LinkedList<ScheduleGraphEdge>>> graphEdges, HashMap<Integer, HashMap<Integer, LinkedList<ScheduleGraphEdge>>> footConnectionsEdges, int startStop, int targetStop, String time) {
        long startTime = System.currentTimeMillis();

        Set<Integer> nodeIds = graphEdges.keySet();

        HashMap<Integer, ScheduleGraphNode> nodes = new HashMap<>();
        ScheduleGraphEdge infiniteEdge = new ScheduleGraphEdge(-1, -1, -1, -1, "23:59", "23:59");
        ScheduleGraphEdge zeroEdge = new ScheduleGraphEdge(-1, -1, -1, startStop, time, time);
        ScheduleGraphNode source = new ScheduleGraphNode(startStop, infiniteEdge);

        for (Integer nodeId : nodeIds) {
            ScheduleGraphNode node;
            if (nodeId == startStop) {
                node = new ScheduleGraphNode(nodeId, zeroEdge);
                source = node;
            } else {
                node = new ScheduleGraphNode(nodeId, infiniteEdge);
            }

            nodes.put(nodeId, node);
        }

        PriorityQueue<EdgeSequence> unsettledNodes = new PriorityQueue<>();
        HashSet<ScheduleGraphNode> settledNodes = new HashSet<>();
        ScheduleGraphNode finalNode = null;
        List<EdgeSequence> settledEdgeSequences = new ArrayList<>();

        EdgeSequence sourceSequence = new EdgeSequence();
        sourceSequence.addEdge(zeroEdge);

        unsettledNodes.add(sourceSequence);

        while (!unsettledNodes.isEmpty()) {
            EdgeSequence currentEdgeSequence = unsettledNodes.poll();
            int currentStop = currentEdgeSequence.getLast().getTargetStop();
            String currentStopArrival = currentEdgeSequence.getLast().getArrivalTime();

            if (!graphEdges.containsKey(currentStop)) continue;
            for (LinkedList<ScheduleGraphEdge> multiEdge : graphEdges.get(currentStop).values()) {

                for (Iterator<ScheduleGraphEdge> i = multiEdge.iterator(); i.hasNext(); ) {
                    ScheduleGraphEdge adjacentEdge = i.next();

                    //only process edges departure time higher than current node distance
                    int compareResult = adjacentEdge.getDepartureTime().compareTo(currentStopArrival);
                    if (compareResult < 0) continue;

                    if (compareResult == 0 && adjacentEdge.getLineId() != currentEdgeSequence.getLast().getLineId())
                        continue;

                    //add first suitable edge - departure time is > current stop arrival
                    List<ScheduleGraphEdge> edgesToProcess = new ArrayList<>();
                    edgesToProcess.add(adjacentEdge);

                    //get all edges with the same departure time as first suitable one
                    while (i.hasNext()) {
                        ScheduleGraphEdge sameTimeAdjacentEdge = i.next();

                        if (!sameTimeAdjacentEdge.getDepartureTime().equals(adjacentEdge.getDepartureTime())) {
                            break;
                        }

                        //ADDED
                        edgesToProcess.add(sameTimeAdjacentEdge);
                    }

                    //find if there is not foot connection available
                    if (footConnectionsEdges.containsKey(currentStop)) {
                        for (LinkedList<ScheduleGraphEdge> multiFootEdge : footConnectionsEdges.get(currentStop).values()) {
                            for (Iterator<ScheduleGraphEdge> j = multiFootEdge.iterator(); j.hasNext(); ) {
                                ScheduleGraphEdge footEdge = new ScheduleGraphEdge(j.next());
                                DateTime d = DateTime.parse(currentStopArrival, DateTimeFormat.forPattern(getTimeFormat()));

                                //add foot connection time to arrival time of current stop
                                d = d.plusMinutes(footEdge.getTime());

                                String footArrival = d.toString(getTimeFormat());
                                footEdge.setDepartureTime(currentStopArrival);
                                footEdge.setArrivalTime(footArrival);

                                edgesToProcess.add(footEdge);
                            }
                        }
                    }

                    //ADDED
                    for (ScheduleGraphEdge ed : edgesToProcess) {
                        int target = ed.getTargetStop();
                        if (!nodes.containsKey(target)) {
                            continue;
                        }

                        //if already contains given edge, skip it
                        if (currentEdgeSequence.containsEdge(ed)) {
                            continue;
                        }

                        ScheduleGraphNode adjacentNode = nodes.get(target);


                        EdgeSequence newSequence = null;
                        if (!settledNodes.contains(adjacentNode)) {
                            settledNodes.add(adjacentNode);
                            adjacentNode.setPrecedingEdge(ed);
                            //currentEdgeSequence.addEdge(ed);

                            newSequence = new EdgeSequence(currentEdgeSequence);
                            newSequence.addEdge(ed);
                            adjacentNode.setEdgeSequence(newSequence);
                        } else {
                            EdgeSequence adjacentNodeSequence = adjacentNode.getEdgeSequence();
                            String adjacentNodeDistance = adjacentNode.getDistance();

                            newSequence = new EdgeSequence(currentEdgeSequence);
                            newSequence.addEdge(ed);
                            if (/*(ed.getArrivalTime().compareTo(adjacentNodeDistance) < 0) ||
                                    (ed.getArrivalTime().compareTo(adjacentNodeDistance) == 0 && adjacentNodeSequence.getLineCount() > newSequence.getLineCount())*/
                                    newSequence.compareTo(adjacentNodeSequence) < 0) {
                                adjacentNode.setPrecedingEdge(ed);
                                adjacentNode.setEdgeSequence(newSequence);
                            } else {
                                newSequence = null;
                            }
                        }

                        if (target == targetStop && newSequence != null) {
                            settledEdgeSequences.add(newSequence);
                        } else {
                            if (newSequence != null) {
                                unsettledNodes.add(newSequence);
                            }
                        }
                    }

                    //only process first suitable edge and edges with same departure time -> break from cycle
                    break;
                }
            }
        }

        if (settledEdgeSequences.isEmpty()) {
            return new ArrayList<>();
        }

        Collections.sort(settledEdgeSequences, new Comparator<EdgeSequence>() {
            @Override
            public int compare(EdgeSequence o1, EdgeSequence o2) {
                //return o1.getLastTime().compareTo(o2.getLastTime());
                return o1.compareTo(o2);
            }
        });


        /*for (EdgeSequence emp : settledEdgeSequences) {
            StringBuilder b = new StringBuilder();
            b.append(emp.getLast().getLineName());
            b.append(" ");
            b.append(emp.getLast().getTargetStopName());
            b.append(" ");
            b.append(emp.getLast().getArrivalTime());
            Log.d("dbg", b.toString());
        }
        Log.d("dbg", "=====================================");*/


        ArrayList<BusStopDeparture> result = new ArrayList<>();


        int i = 0;
        List<ScheduleGraphEdge> resultEdges = settledEdgeSequences.get(0).getEdges();
        ScheduleGraphEdge edge = resultEdges.get(i);

        i++;
        while (i < resultEdges.size()) {
            ScheduleGraphEdge nextEdge = resultEdges.get(i);

            if (edge.getLineId() != -1) {
                if (nextEdge.getLineId() != edge.getLineId()) {
                    result.add(new BusStopDeparture(edge.getLineName(), edge.getTargetStopName(), edge.getArrivalTime(), edge.getLineId(), edge.getConnectionId()));
                    result.add(new BusStopDeparture(nextEdge.getLineName(), nextEdge.getStartStopName(), nextEdge.getDepartureTime(), nextEdge.getLineId(), nextEdge.getConnectionId()));
                }
            } else {
                result.add(new BusStopDeparture(nextEdge.getLineName(), nextEdge.getStartStopName(), nextEdge.getDepartureTime(), nextEdge.getLineId(), nextEdge.getConnectionId()));
            }


            /*if(nextEdge.getLineId() != edge.getLineId() && edge.getLineId() != -1){
                result.add(new BusStopDeparture(nextEdge.getLineName(), nextEdge.getTargetStopName(), nextEdge.getArrivalTime(), nextEdge.getLineId(), nextEdge.getConnectionId()));
            }*/
            edge = nextEdge;
            i++;
        }
        result.add(new BusStopDeparture(edge.getLineName(), edge.getTargetStopName(), edge.getArrivalTime(), edge.getLineId(), edge.getConnectionId()));


        return result;
    }

    @Override
    public Class getObjectClass() {
        return Connection.class;
    }

    /**
     * retrieves departure graph representation and caches it in local variable
     * @return departure graph representation
     */
    private HashMap<Integer, HashMap<Integer, LinkedList<ScheduleGraphEdge>>> getGraphEdges() {
        String[] codes = getCodesForDate(getDate());
        if (graphEdges == null) {
            graphEdges = getDb().getScheduleGraph(getTime(), getDate(), codes);
        }

        return graphEdges;
    }

    /**
     * retrieves foot connections graph representation from db and caches it in local variable
     * @return foot connections graph representation
     */
    private HashMap<Integer, HashMap<Integer, LinkedList<ScheduleGraphEdge>>> getFootConnectionsEdges() {

        if (footConnectionsEdges == null) {
            footConnectionsEdges = getDb().getFootConnections();
        }

        return footConnectionsEdges;
    }

    public List<Connection> exec(int page) {
        ArrayList<Connection> result = new ArrayList<>();

        //Dijkstra(getGraphEdges(), getFootConnectionsEdges(), getStartStopId(), getTargetStopId(), getTime());

        for (int i = 0; i < 10; i++) {
            if (foundConnections.isEmpty()) {
                Connection newConnection = new Connection(Dijkstra(getGraphEdges(), getFootConnectionsEdges(), getStartStopId(), getTargetStopId(), getTime()));

                //if Dijkstra returns empty array
                if (newConnection.getDepartures().isEmpty()) {
                    break;
                }

                result.add(newConnection);
                foundConnections.add(newConnection);
            } else {
                Connection lastConnection = foundConnections.get(foundConnections.size() - 1);
                String lastFoundDeparture = lastConnection.get(0).getDeparture();

                DateTime time = DateTime.parse(lastFoundDeparture, DateTimeFormat.forPattern(getTimeFormat()));
                time = time.plusMinutes(1);

                Connection newConnection = new Connection(Dijkstra(getGraphEdges(), getFootConnectionsEdges(), getStartStopId(), getTargetStopId(), time.toString(getTimeFormat())));
                if (newConnection.getDepartures().isEmpty()) {
                    break;
                }
                result.add(newConnection);
                foundConnections.add(newConnection);
            }
        }

        return result;
    }

    public void execAndDisplayResult() {

        Bundle bundle = new Bundle();
        Intent intent;

        foundConnections.clear();
        graphEdges = null;

        intent = new Intent(mContext.getApplicationContext(), DeparturesActivity.class);

        bundle.putSerializable("com.android.dpmjinfo.queryModel", model);
        bundle.putSerializable("com.android.dpmjinfo.queryClass", this.getClass().getSimpleName());
        intent.putExtras(bundle);

        //start given activity
        mContext.startActivity(intent);
    }

    /**
     * gets initial date for query
     * @return date as formatted string
     */
    public String getInitialDate() {
        /*DateFormat format = new SimpleDateFormat(getDateFormat());
        Date date = new Date();*/
        DateTime d = DateTime.now();

        return d.toString(getDateFormat());//format.format(date);
    }

    /**
     * gets initial time for query
     * @return time as formatted string
     */
    public String getInitialTime() {
        /*DateFormat format = new SimpleDateFormat(getTimeFormat());
        Date date = new Date();
        return format.format(date);*/
        DateTime d = DateTime.now();

        return d.toString(getTimeFormat());
    }

    private void notifyStartStopsChanged() {
        view.onStartStopsUpdated();
    }

    private void notifyTargetStopsChanged() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.onTargetStopsUpdated();
            }
        });
    }

    private void notifyTargetStopsChanged(int positionToSelect) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.onTargetStopsUpdated(positionToSelect);
            }
        });
    }

    public List<BusStop> getStartStops() {
        return startStops;
    }

    public List<BusStop> getTargetStops() {
        return targetStops;
    }

    @Override
    public BaseAdapter getAdapter() {
        return new ConnectionAdapter(R.layout.connection_list_item);
    }

    public StartStopSelectedListener getStartStopSelectedListener() {
        return new StartStopSelectedListener(this);
    }

    private void onStartStopSelected(int position) {
        setStartStop(startStops.get(position));

        Log.d("dbg", "start stop");

        int positionToSelect = 0;

        if (removedTargetStop != null) {
            targetStops.add(removedTargetStopPosition, removedTargetStop);
        }

        removedTargetStop = targetStops.get(position);
        targetStops.remove(position);
        removedTargetStopPosition = position;

        //find position of currently selected target stop
        int i;
        for (i = 0; i < targetStops.size(); i++) {
            if (targetStops.get(i).getCISId() == getTargetStopId()) {
                positionToSelect = i;
                break;
            }
        }

        //if not found then it was removed as it was selected as start stop -> choose first target stop
        if (positionToSelect >= targetStops.size()) {
            positionToSelect = 0;
        }

        final int finalPosition = positionToSelect;
        Log.d("dbg", "position: " + finalPosition);

        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyTargetStopsChanged(finalPosition);
            }
        });

    }



    public TargetStopSelectedListener getTargetStopSelectedListener() {
        return new TargetStopSelectedListener(this);
    }

    private void onTargetStopSelected(int position) {
        setTargetStop(targetStops.get(position));
        Log.d("dbg", "target selected " + getModel().getTargetStop().getName());
    }

    private class StartStopSelectedListener implements AdapterView.OnItemSelectedListener {
        ConnectionQuery query;

        public StartStopSelectedListener(ConnectionQuery q) {
            query = q;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            query.onStartStopSelected(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private class TargetStopSelectedListener implements AdapterView.OnItemSelectedListener {
        ConnectionQuery query;

        public TargetStopSelectedListener(ConnectionQuery q) {
            query = q;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            query.onTargetStopSelected(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public ArrayList<String> getRequiredFileTypes() {
        ArrayList<String> required = new ArrayList<>();
        required.add(OfflineFilesManager.SCHEDULE);
        required.add(OfflineFilesManager.CALENDAR);

        return required;
    }
}
