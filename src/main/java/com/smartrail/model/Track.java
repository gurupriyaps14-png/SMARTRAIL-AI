package com.smartrail.model;

import java.io.Serializable;

/**
 * Model representing a physical railway track / line at a station block.
 */
public class Track implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int stationId;
    private int trackNumber;
    private String trackName;
    private String trackType;       // MAIN_UP, MAIN_DOWN, LOOP_1, LOOP_2, FREIGHT_SIDING
    private int maxSpeedKmh;
    private String status;          // CLEAR, OCCUPIED, RESERVED, MAINTENANCE
    private int lengthMeters;
    private String currentTrainNumber;
    private String currentTrainName;

    public Track() {}

    public Track(int id, int stationId, int trackNumber, String trackName, String trackType,
                 int maxSpeedKmh, String status, int lengthMeters, String currentTrainNumber) {
        this.id = id;
        this.stationId = stationId;
        this.trackNumber = trackNumber;
        this.trackName = trackName;
        this.trackType = trackType;
        this.maxSpeedKmh = maxSpeedKmh;
        this.status = status;
        this.lengthMeters = lengthMeters;
        this.currentTrainNumber = currentTrainNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getTrackType() {
        return trackType;
    }

    public void setTrackType(String trackType) {
        this.trackType = trackType;
    }

    public int getMaxSpeedKmh() {
        return maxSpeedKmh;
    }

    public void setMaxSpeedKmh(int maxSpeedKmh) {
        this.maxSpeedKmh = maxSpeedKmh;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getLengthMeters() {
        return lengthMeters;
    }

    public void setLengthMeters(int lengthMeters) {
        this.lengthMeters = lengthMeters;
    }

    public String getCurrentTrainNumber() {
        return currentTrainNumber;
    }

    public void setCurrentTrainNumber(String currentTrainNumber) {
        this.currentTrainNumber = currentTrainNumber;
    }

    public String getCurrentTrainName() {
        return currentTrainName;
    }

    public void setCurrentTrainName(String currentTrainName) {
        this.currentTrainName = currentTrainName;
    }
}

