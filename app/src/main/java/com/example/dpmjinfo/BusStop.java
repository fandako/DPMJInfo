package com.example.dpmjinfo;

import androidx.annotation.NonNull;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.layers.IntegratedMeshLayer;

import java.io.Serializable;
import java.util.Map;

import javax.crypto.Cipher;

/* AVAILABLE FIELDS FROM ARCGIS FEATURE
objectid ( type: esriFieldTypeOID, alias: OBJECTID )
odkaz ( type: esriFieldTypeString, alias: ODKAZ, length: 100 )
linky ( type: esriFieldTypeString, alias: linky, length: 25 )
nazev ( type: esriFieldTypeString, alias: název, length: 25 )
zastavka ( type: esriFieldTypeString, alias: zastávka, length: 30 )
stav ( type: esriFieldTypeString, alias: stav, length: 30 )
oznacnik ( type: esriFieldTypeString, alias: označník, length: 50 , Coded Values: [na sloupu: na sloupu] , [samostatný: samostatný] , [součástí přístřešku: součástí přístřešku] )
lavicka ( type: esriFieldTypeString, alias: lavička, length: 50 , Coded Values: [Ne: Ne] , [Součástí přístřešku: Součástí přístřešku] , [Volně stojící: Volně stojící] , ...1 more... )
kos ( type: esriFieldTypeString, alias: koš, length: 3 , Coded Values: [ano: ano] , [ne: ne] )
pristresek ( type: esriFieldTypeString, alias: přístřešek, length: 3 , Coded Values: [ano: ano] , [ne: ne] )
vlastnikpr ( type: esriFieldTypeString, alias: vlastník přístřešku, length: 50 , Coded Values: [DP Jihlava: DP Jihlava] , [Statutární město Jihlava: Statutární město Jihlava] , [RENCAR: RENCAR] , ...3 more... )
jine ( type: esriFieldTypeString, alias: jiné, length: 30 )
pozemek ( type: esriFieldTypeString, alias: na pozemku, length: 30 , Coded Values: [Statutární město Jihlava: Statutární město Jihlava] , [Kraj Vysočina: Kraj Vysočina] , [Jiný: Jiný] )
poznamka ( type: esriFieldTypeString, alias: poznámka, length: 250 )
k_oprave ( type: esriFieldTypeString, alias: k opravě, length: 50 )
k_reseni ( type: esriFieldTypeString, alias: k řešení, length: 20 )
bezbar ( type: esriFieldTypeString, alias: bezbariérová, length: 50 , Coded Values: [Ano: Ano] , [Ne: Ne] , [Ano částečně: Ano částečně] )
shape ( type: esriFieldTypeGeometry, alias: Shape )
kontrola ( type: esriFieldTypeDate, alias: datum kontroly, length: 8 )
globalid ( type: esriFieldTypeGlobalID, alias: GlobalID, length: 38 )
vlastnik_oznacniku ( type: esriFieldTypeString, alias: Vlastnik_oznacniku, length: 100 , Coded Values: [Dopravní podnik: Dopravní podnik] , [Statutární město Jihlava: Statutární město Jihlava] , [Jiný: Jiný] )
linky_vld ( type: esriFieldTypeString, alias: Linky_VLD, length: 300 )
dopravci_vld ( type: esriFieldTypeString, alias: Dopravci_VLD, length: 300 )
oznacnik_vld ( type: esriFieldTypeString, alias: Oznacnik_VLD, length: 100 , Coded Values: [Samostatný ICOM: Samostatný ICOM] , [Společný DP: Společný DP] )
rok_opravy ( type: esriFieldTypeSmallInteger, alias: Rok_opravy )
vlastnikpr_poznamka ( type: esriFieldTypeString, alias: VLASTNIKPR_poznamka, length: 50 )
bezbar_poznamka ( type: esriFieldTypeString, alias: bezbar_poznamka, length: 50 )
vlastnik_oznacniku_poznamka ( type: esriFieldTypeString, alias: Vlastnik_oznacniku_poznamka, length: 50 )
na_pozemku_poznamka ( type: esriFieldTypeString, alias: na_pozemku_poznamka, length: 50 )
oznacnik_vld_poznamka ( type: esriFieldTypeString, alias: Oznacnik_VLD_poznamka, length: 50 )
elp_id ( type: esriFieldTypeSmallInteger, alias: ELP_ID )
created_user ( type: esriFieldTypeString, alias: created_user, length: 255 )
created_date ( type: esriFieldTypeDate, alias: created_date, length: 8 )
last_edited_user ( type: esriFieldTypeString, alias: last_edited_user, length: 255 )
last_edited_date ( type: esriFieldTypeDate, alias: last_edited_date, length: 8 )
*/

public class BusStop implements Serializable {
    private String href;
    private String lines;
    private String name;
    /*private String stop;
    private String state;
    private String oznacnik;
    private String bench;
    private String kos;
    private String shelter;*/
    private String wheelchairAccessible;
    private Short elp_id;

    private int CISId;

    public BusStop(Feature f) {
        Map<String, Object> attributes = f.getAttributes();
        href = (String) attributes.get("odkaz");
        lines = (String) attributes.get("linky");
        name = (String) attributes.get("nazev");
        wheelchairAccessible = (String) attributes.get("bezbar");
        elp_id = (Short) attributes.get("elp_id");
    }

    public BusStop(int CISId, String name) {
        this.CISId = CISId;
        this.name = name;
    }

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

    /*public String getStop() {
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
    }*/

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

    public int getCISId() {
        return CISId;
    }

    public void setCISId(int CISId) {
        this.CISId = CISId;
    }

    @Override
    public int hashCode() {
        if (elp_id != null) {
            return getElp_id();
        } else {
            return getCISId();
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other.getClass() != getClass()){
            return false;
        }

        if (elp_id != null) {
            if (getElp_id() == ((BusStop) other).getElp_id()) {
                return true;
            }
        } else {
            if (getCISId() == ((BusStop) other).getCISId()) {
                return true;
            }
        }

        return false;
    }

    //has effect on how busStops are displayed in searchable spinner!!!
    @NonNull
    @Override
    public String toString() {
        return getName();
    }
}
