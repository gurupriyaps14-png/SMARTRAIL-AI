package com.smartrail.model;

import java.io.Serializable;

/**
 * Model representing a detected track occupancy contention between two trains.
 */
public class Conflict implements Serializable {
    private static final long serialVersionUID = 1L;

    private String conflictId;
    private int stationId;
    private String stationCode;
    private int trackNumber;
    private String trackName;
    private Train train1;
    private Train train2;
    private String conflictType; // SAME_TRACK_WINDOW, OPPOSING_TRANSIT, HEAD_ON_RISK, BOTTLENECK
    private String severity;     // CRITICAL, HIGH, MODERATE
    private int timeWindowOverlapMinutes;
    private String status;       // DETECTED, PENDING_APPROVAL, RESOLVED, HELD
    private String timestamp;
    private DiversionRecommendation recommendation;

    public Conflict() {
        this.status = "DETECTED";
        this.severity = "HIGH";
    }

    public String getConflictId() {
        return conflictId;
    }

    public void setConflictId(String conflictId) {
        this.conflictId = conflictId;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public String getStationCode() {
        return stationCode;
    }

    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
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

    public Train getTrain1() {
        return train1;
    }

    public void setTrain1(Train train1) {
        this.train1 = train1;
    }

    public Train getTrain2() {
        return train2;
    }

    public void setTrain2(Train train2) {
        this.train2 = train2;
    }

    public String getConflictType() {
        return conflictType;
    }

    public void setConflictType(String conflictType) {
        this.conflictType = conflictType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public int getTimeWindowOverlapMinutes() {
        return timeWindowOverlapMinutes;
    }

    public void setTimeWindowOverlapMinutes(int timeWindowOverlapMinutes) {
        this.timeWindowOverlapMinutes = timeWindowOverlapMinutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public DiversionRecommendation getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(DiversionRecommendation recommendation) {
        this.recommendation = recommendation;
    }
}

