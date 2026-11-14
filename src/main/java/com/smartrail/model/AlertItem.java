package com.smartrail.model;

import java.io.Serializable;

/**
 * Model representing a real-time event or alert in the SmartRail AI telemetry feed.
 */
public class AlertItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String type;      // CONFLICT_DETECTED, TRAIN_DIVERTED, TRAIN_DELAYED, TRAIN_APPROACHING, DESTINATION_REACHED, ALTERNATIVE_UNAVAILABLE
    private String title;
    private String message;
    private String severity;  // RED, AMBER, BLUE, GREEN, TEAL
    private String timestamp;
    private String trainNumber;
    private int trackNumber;
    private String stationCode;

    public AlertItem() {}

    public AlertItem(String id, String type, String title, String message, String severity,
                     String timestamp, String trainNumber, int trackNumber, String stationCode) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.message = message;
        this.severity = severity;
        this.timestamp = timestamp;
        this.trainNumber = trainNumber;
        this.trackNumber = trackNumber;
        this.stationCode = stationCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }

    public String getStationCode() {
        return stationCode;
    }

    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }
}

