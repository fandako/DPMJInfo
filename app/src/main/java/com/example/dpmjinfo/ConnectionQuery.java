package com.example.dpmjinfo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.example.dpmjinfo.activities.Departures;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class ConnectionQuery extends ScheduleQuery {
    private CISSqliteHelper mDb = null;
    private List<BusStop> targetStops;
    private List<BusStop> startStops;
    private ConnectionQueryModel model;
    private List<Connection> foundConnections;
    private HashMap<Integer, HashMap<Integer, LinkedList<ScheduleGraphEdge>>> graphEdges = null;

    private BusStop removedTargetStop = null;
    private int removedTargetStopPosition;


    private static final int DEFAULT_PAGE_SIZE = 10;

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
        return model.getStartStop();
    }

    public void setStartStopId(int stopId) {
        model.setStartStop(stopId);
    }

    public int getTargetStopId() {
        return model.getTargetStop();
    }

    public void setTargetStopId(int stopId) {
        model.setTargetStop(stopId);
    }

    public String getDate() {
        return model.getDate();
    }

    public void setDate(String date) {
        model.setDate(date);
    }

    public String getTime() {
        return model.getTime();
    }

    public void setTime(String time) {
        model.setTime(time);
    }

    @Override
    public int getPageSize() {
        return model.getPageSize();
    }

    public void setPageSize(int pageSize) {
        model.setPageSize(pageSize);
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
        startStops.addAll(busStops);
        targetStops.addAll(busStops);

        notifyStartStopsChanged();
        notifyTargetStopsChanged();
    }

    public String getName() {
        return "Spojení";
    }

    protected void initView(View v) {

    }

    @Override
    public boolean isAsync() {
        return true;
    }

    private List<BusStopDeparture> Dijkstra(HashMap<Integer, HashMap<Integer, LinkedList<ScheduleGraphEdge>>> graphEdges, int startStop, int targetStop, String time) {
        long startTime = System.currentTimeMillis();

        Set<Integer> nodeIds = graphEdges.keySet();

        HashMap<Integer, ScheduleGraphNode> nodes = new HashMap<>();
        ScheduleGraphEdge infiniteEdge = new ScheduleGraphEdge(-1, -1, -1, -1, "23:59", "23:59");
        ScheduleGraphEdge zeroEdge = new ScheduleGraphEdge(-1, -1, -1, -1, time, time);
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

        PriorityQueue<ScheduleGraphNode> unsettledNodes = new PriorityQueue<>();
        HashSet<ScheduleGraphNode> settledNodes = new HashSet<>();
        ScheduleGraphNode finalNode = null;

        unsettledNodes.add(source);

        while (!unsettledNodes.isEmpty()) {
            ScheduleGraphNode currentNode = unsettledNodes.poll();

            if (!graphEdges.containsKey(currentNode.getStopId())) continue;
            for (LinkedList<ScheduleGraphEdge> multiEdge : graphEdges.get(currentNode.getStopId()).values()) {
                //ScheduleGraphEdge adjacentEdge = multiEdge.getFirst();
                for (Iterator<ScheduleGraphEdge> i = multiEdge.iterator(); i.hasNext();){
                    ScheduleGraphEdge adjacentEdge = i.next();

                    //only process edges departure time higher than current node distance
                    int compareResult = adjacentEdge.getDepartureTime().compareTo(currentNode.getDistance());
                    if (compareResult < 0 /*|| compareResult == 0*/) continue;

                    while (i.hasNext()){
                        ScheduleGraphEdge sameTimeAdjacentEdge = i.next();

                        if(!sameTimeAdjacentEdge.getDepartureTime().equals(adjacentEdge.getDepartureTime())){
                            break;
                        }

                        //prefer same line for edges with same departure time
                        if(sameTimeAdjacentEdge.getLineId() == currentNode.getPrecedingEdge().getLineId()){
                            adjacentEdge = sameTimeAdjacentEdge;
                        }
                    }

                    //filtration by time to reduce number of edges to process may cause
                    //that some nodes(stops) will be missing as there are no departures from them after given time
                    int target = adjacentEdge.getTargetStop();
                    if(!nodes.containsKey(target)){
                        continue;
                    }

                    ScheduleGraphNode adjacentNode = nodes.get(target);

                    if (!settledNodes.contains(adjacentNode)) {
                        if (adjacentEdge.getArrivalTime().compareTo(adjacentNode.getDistance()) < 0) {
                            adjacentNode.setPrecedingEdge(adjacentEdge);
                        }
                        unsettledNodes.add(adjacentNode);
                    }

                    break;
                }
            }
            if (currentNode.getStopId() == targetStop) {
                finalNode = currentNode;
                break;
            }
            settledNodes.add(currentNode);
        }

        if (finalNode == null) {
            return new ArrayList<>();
        } else {
            ScheduleGraphEdge edge = finalNode.getPrecedingEdge();
            ArrayList<BusStopDeparture> result = new ArrayList<>();

            result.add(new BusStopDeparture(edge.getLineName(), edge.getTargetStopName(), edge.getArrivalTime(), edge.getLineId(), edge.getConnectionId()));

            while (edge.getStartStop() != -1) {
                result.add(new BusStopDeparture(edge.getLineName(), edge.getStartStopName(), edge.getDepartureTime(), edge.getLineId(), edge.getConnectionId()));
                ScheduleGraphEdge nextEdge = nodes.get(edge.getStartStop()).getPrecedingEdge();
                if(nextEdge.getLineId() != edge.getLineId() && nextEdge.getLineId() != -1){
                    result.add(new BusStopDeparture(nextEdge.getLineName(), nextEdge.getTargetStopName(), nextEdge.getArrivalTime(), nextEdge.getLineId(), nextEdge.getConnectionId()));
                }
                edge = nextEdge;
            }

            Collections.reverse(result);
            long endTime = System.currentTimeMillis();
            Log.d("dbg time", (endTime - startTime) + " ms");
            return result;
        }

    }

    @Override
    public Class getObjectClass() {
        return Connection.class;
    }

    private HashMap<Integer, HashMap<Integer, LinkedList<ScheduleGraphEdge>>> getGraphEdges() {
        String[] codes = getCodesForDate(getDate());
        if(graphEdges == null){
            graphEdges = getDb().getScheduleGraph(getTime(), getDate(), codes);
        }

        return graphEdges;
    }

    public List<Connection> exec(int page) {
        ArrayList<Connection> result = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            if(foundConnections.isEmpty()){
                Connection newConnection = new Connection(Dijkstra(getGraphEdges(), getStartStopId(), getTargetStopId(), getTime()));

                //if Dijkstra returns empty array
                if(newConnection.getDepartures().isEmpty()){
                    break;
                }

                result.add(newConnection);
                foundConnections.add(newConnection);
            } else {
                Connection lastConnection = foundConnections.get(foundConnections.size() - 1);
                String lastFoundDeparture = lastConnection.get(0).getDeparture();

                DateTime time = DateTime.parse(lastFoundDeparture, DateTimeFormat.forPattern(getTimeFormat()));
                time = time.plusMinutes(1);

                Connection newConnection = new Connection(Dijkstra(getGraphEdges(), getStartStopId(), getTargetStopId(), time.toString(getTimeFormat())/*format.format(c.getTime())*/));
                if(newConnection.getDepartures().isEmpty()){
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

        intent = new Intent(mContext.getApplicationContext(), Departures.class);

        bundle.putSerializable("com.android.dpmjinfo.queryModel", model);
        bundle.putSerializable("com.android.dpmjinfo.queryClass", this.getClass().getSimpleName());
        intent.putExtras(bundle);

        //start given activity
        mContext.startActivity(intent);
    }

    public String getInitialDate() {
        /*DateFormat format = new SimpleDateFormat(getDateFormat());
        Date date = new Date();*/
        DateTime d = DateTime.now();

        return d.toString(getDateFormat());//format.format(date);
    }

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
        view.onTargetStopsUpdated();
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
        setStartStopId(startStops.get(position).getCISId());

        if(removedTargetStop != null){
            targetStops.add(removedTargetStopPosition, removedTargetStop);
        }

        removedTargetStop = targetStops.get(position);
        targetStops.remove(position);
        removedTargetStopPosition = position;
        notifyTargetStopsChanged();
    }

    public TargetStopSelectedListener getTargetStopSelectedListener() {
        return new TargetStopSelectedListener(this);
    }

    private void onTargetStopSelected(int position) {
        setTargetStopId(targetStops.get(position).getCISId());
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
