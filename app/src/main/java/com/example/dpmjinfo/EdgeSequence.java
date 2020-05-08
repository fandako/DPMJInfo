package com.example.dpmjinfo;

import com.example.dpmjinfo.queries.ScheduleQuery;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class EdgeSequence implements Comparable<EdgeSequence> {
    private List<ScheduleGraphEdge> edges;
    private HashMap<Integer, HashSet<Integer>> contains;
    private HashSet<Integer> lines;
    private int waiting;

    private String lastTime;

    public EdgeSequence() {
        edges = new ArrayList<>();
        contains = new HashMap<>();
        lines = new HashSet<>();
        waiting = 0;
    }

    public EdgeSequence(EdgeSequence o) {
        this();

        addAllEdge(o.getEdges());
    }

    public boolean containsEdge(ScheduleGraphEdge e) {
        boolean containsStart = contains.containsKey(e.getStartStop());
        boolean containsTarget = contains.containsKey(e.getTargetStop());
        if (!containsStart && !containsTarget) {
            return false;
        }

        if (containsStart) {
            return contains.get(e.getStartStop()).contains(e.getTargetStop());
        }

        return contains.get(e.getTargetStop()).contains(e.getStartStop());
    }

    public void addEdge(ScheduleGraphEdge e) {
        Integer startStop = e.getStartStop();
        Integer targetStop = e.getTargetStop();

        if (contains.containsKey(startStop)) {
            contains.get(startStop).add(targetStop);
        } else {
            HashSet<Integer> newSet = new HashSet<>();
            newSet.add(targetStop);

            contains.put(startStop, newSet);
        }

        //if change of line occurs add waiting time to overall waiting time
        if (!edges.isEmpty()) {
            ScheduleGraphEdge last = getLast();

            if (last.getLineId() != e.getLineId()) {
                String timeFormat = ScheduleQuery.getTimeFormat();
                DateTime departureTime = DateTime.parse(last.getArrivalTime(), DateTimeFormat.forPattern(timeFormat));
                DateTime arrivalTime = DateTime.parse(e.getDepartureTime(), DateTimeFormat.forPattern(timeFormat));

                Period period = new Period(departureTime, arrivalTime);

                long diffMinutes = period.getMinutes();
                waiting += diffMinutes;
            }
        }

        edges.add(e);
        lines.add(e.getLineId());
    }

    public int getLineCount() {
        return lines.size();
    }

    public void addAllEdge(List<ScheduleGraphEdge> es) {
        for (ScheduleGraphEdge e : es) {
            addEdge(e);
        }
    }

    public List<ScheduleGraphEdge> getEdges() {
        return edges;
    }

    public int getWaiting() {
        return waiting;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        EdgeSequence other = (EdgeSequence) obj;
        if (!edges.equals(other.edges)) return false;

        return getLastTime().compareTo(other.getLastTime()) == 0;
    }

    @Override
    public int compareTo(EdgeSequence o) {

        int compareResult = getLastTime().compareTo(o.getLastTime());
        if (compareResult == 0) {
            compareResult = Integer.compare(getLineCount(), o.getLineCount());
            if (compareResult == 0) {
                return Integer.compare(getWaiting(), o.getWaiting());
            }
        }

        return compareResult;
    }

    public String getLastTime() {
        return getLast().getArrivalTime();
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public ScheduleGraphEdge getLast() {
        return edges.get(edges.size() - 1);
    }
}