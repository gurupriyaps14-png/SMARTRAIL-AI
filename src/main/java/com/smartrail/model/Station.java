package com.smartrail.model;

import java.io.Serializable;

/**
 * Model representing a Railway Station in Tamil Nadu.
 */
public class Station implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String code;            // e.g. MAS, MS, TBM, KPD, CBE, TPJ, MDU
    private String name;            // Puratchi Thalaivar Dr. MGR Chennai Central
    private int divisionId;
    private String divisionCode;
    private int platformsCount;
    private int tracksCount;
    private String junctionType;
    private double latitude;
    private double longitude;
    private boolean majorJunction;

    public Station() {}

    public Station(int id, String code, String name, int divisionId, String divisionCode,
                   int platformsCount, int tracksCount, String junctionType,
                   double latitude, double longitude, boolean majorJunction) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.divisionId = divisionId;
        this.divisionCode = divisionCode;
        this.platformsCount = platformsCount;
        this.tracksCount = tracksCount;
        this.junctionType = junctionType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.majorJunction = majorJunction;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(int divisionId) {
        this.divisionId = divisionId;
    }

    public String getDivisionCode() {
        return divisionCode;
    }

    public void setDivisionCode(String divisionCode) {
        this.divisionCode = divisionCode;
    }

    public int getPlatformsCount() {
        return platformsCount;
    }

    public void setPlatformsCount(int platformsCount) {
        this.platformsCount = platformsCount;
    }

    public int getTracksCount() {
        return tracksCount;
    }

    public void setTracksCount(int tracksCount) {
        this.tracksCount = tracksCount;
    }

    public String getJunctionType() {
        return junctionType;
    }

    public void setJunctionType(String junctionType) {
        this.junctionType = junctionType;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isMajorJunction() {
        return majorJunction;
    }

    public void setMajorJunction(boolean majorJunction) {
        this.majorJunction = majorJunction;
    }
}

