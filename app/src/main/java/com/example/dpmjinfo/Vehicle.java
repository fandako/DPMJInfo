package com.example.dpmjinfo;

import java.io.Serializable;
import java.util.Map;

/*  AVAILABLE FIELDS FROM API
objectid ( type: esriFieldTypeOID, alias: objectid )
carnum ( type: esriFieldTypeSmallInteger, alias: carnum )
delayinmins ( type: esriFieldTypeSmallInteger, alias: delayinmins )
finalstopid ( type: esriFieldTypeSmallInteger, alias: finalstopid )
laststopid ( type: esriFieldTypeSmallInteger, alias: laststopid )
lastpostid ( type: esriFieldTypeSmallInteger, alias: lastpostid )
latitude ( type: esriFieldTypeDouble, alias: latitude )
lineid ( type: esriFieldTypeSmallInteger, alias: lineid )
longitude ( type: esriFieldTypeDouble, alias: longitude )
routeid ( type: esriFieldTypeSmallInteger, alias: routeid )
state ( type: esriFieldTypeSmallInteger, alias: state )
linka ( type: esriFieldTypeString, alias: linka, length: 10 )
konecna ( type: esriFieldTypeString, alias: konecna, length: 255 )
posledni ( type: esriFieldTypeString, alias: posledni, length: 255 )
typ ( type: esriFieldTypeString, alias: typ, length: 10 )
azimut ( type: esriFieldTypeSmallInteger, alias: azimut )
speed ( type: esriFieldTypeSmallInteger, alias: speed )
 */

public class Vehicle implements Serializable {

    private Short carNum;
    private Short delayInMins;
    private Short finalStopID;
    private Short lastStopID;
    private Short lastPostID;
    private Double latitude;
    private Double longitude;
    private Short routeID;
    private Short state;
    private String line;
    private String terminalStop;
    private String lastStop;
    private String type;
    private Short azimuth;
    private Short speed;

    public Vehicle(Map<String, Object> attributes){
        carNum = (Short) attributes.get("carnum");
        delayInMins = (Short) attributes.get("delayinmins");
        finalStopID = (Short) attributes.get("finalstopid");
        lastStopID = (Short) attributes.get("laststopid");
        lastPostID = (Short) attributes.get("lastpostid");
        latitude = (Double) attributes.get("latitude");
        longitude = (Double) attributes.get("longitude");
        routeID = (Short) attributes.get("routeid");
        state = (Short) attributes.get("state");
        line = (String) attributes.get("linka");
        terminalStop = (String) attributes.get("konecna");
        lastStop = (String) attributes.get("posledni");
        type = (String) attributes.get("typ");
        azimuth = (Short) attributes.get("azimut");
        speed = (Short) attributes.get("speed");
    }

    public Short getCarNum() {
        return carNum;
    }

    public void setCarNum(Short carNum) {
        this.carNum = carNum;
    }

    public Short getDelayInMins() {
        return delayInMins;
    }

    public void setDelayInMins(Short delayInMins) {
        this.delayInMins = delayInMins;
    }

    public Short getFinalStopID() {
        return finalStopID;
    }

    public void setFinalStopID(Short finalStopID) {
        this.finalStopID = finalStopID;
    }

    public Short getLastStopID() {
        return lastStopID;
    }

    public void setLastStopID(Short lastStopID) {
        this.lastStopID = lastStopID;
    }

    public Short getLastPostID() {
        return lastPostID;
    }

    public void setLastPostID(Short lastPostID) {
        this.lastPostID = lastPostID;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Short getRouteID() {
        return routeID;
    }

    public void setRouteID(Short routeID) {
        this.routeID = routeID;
    }

    public Short getState() {
        return state;
    }

    public void setState(Short state) {
        this.state = state;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getTerminalStop() {
        return terminalStop;
    }

    public void setTerminalStop(String terminalStop) {
        this.terminalStop = terminalStop;
    }

    public String getLastStop() {
        return lastStop;
    }

    public void setLastStop(String lastStop) {
        this.lastStop = lastStop;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Short getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(Short azimuth) {
        this.azimuth = azimuth;
    }

    public Short getSpeed() {
        return speed;
    }

    public void setSpeed(Short speed) {
        this.speed = speed;
    }

    public boolean isWaiting(){
        return getSpeed() == 122;
    }
}
