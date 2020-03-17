package com.example.dpmjinfo;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.layers.IntegratedMeshLayer;

import java.io.Serializable;
import java.util.Map;

public class BusStop implements Serializable {

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getLines() {
        return lines;
    }

    public void setLines(String lines) {
        this.lines = lines;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStop() {
        return stop;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getOznacnik() {
        return oznacnik;
    }

    public void setOznacnik(String oznacnik) {
        this.oznacnik = oznacnik;
    }

    public String getBench() {
        return bench;
    }

    public void setBench(String bench) {
        this.bench = bench;
    }

    public String getKos() {
        return kos;
    }

    public void setKos(String kos) {
        this.kos = kos;
    }

    public String getShelter() {
        return shelter;
    }

    public void setShelter(String shelter) {
        this.shelter = shelter;
    }

    public String getWheelchairAccessible() {
        return wheelchairAccessible;
    }

    public void setWheelchairAccessible(String wheelchairAccessible) {
        this.wheelchairAccessible = wheelchairAccessible;
    }

    public Short getElp_id() {
        return elp_id;
    }

    public void setElp_id(Short elp_id) {
        this.elp_id = elp_id;
    }

    private String href;
    private String lines;
    private String name;
    private String stop;
    private String state;
    private String oznacnik;
    private String bench;
    private String kos;
    private String shelter;
    private String wheelchairAccessible;
    private Short elp_id;

    public BusStop(Feature f){
        Map<String, Object> attributes = f.getAttributes();
        href = (String) attributes.get("odkaz");
        lines = (String) attributes.get("linky");
        name = (String) attributes.get("nazev");
        elp_id = (Short) attributes.get("elp_id");
    }

    @Override
    public int hashCode() {
        return getElp_id();
    }

    @Override
    public boolean equals(Object other) {
        if(getElp_id() == ((BusStop)other).getElp_id()){
            return true;
        }

        return false;
    }
}
