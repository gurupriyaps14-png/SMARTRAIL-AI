package com.smartrail.model;

public class AlertNotification {
    private int id;
    private String alertCode;
    private String trainNumber;
    private String conflictType;
    private String timeDetected;
    private String recommendedAction;
    private String dispatchStatus;
    private String severity;
    private String stationCode;
    private int trackNumber;
    private String details;

    public AlertNotification() {}

    public AlertNotification(int id, String alertCode, String trainNumber, String conflictType,
                             String timeDetected, String recommendedAction, String dispatchStatus,
                             String severity, String stationCode, int trackNumber, String details) {
        this.id = id;
        this.alertCode = alertCode;
        this.trainNumber = trainNumber;
        this.conflictType = conflictType;
        this.timeDetected = timeDetected;
        this.recommendedAction = recommendedAction;
        this.dispatchStatus = dispatchStatus;
        this.severity = severity;
        this.stationCode = stationCode;
        this.trackNumber = trackNumber;
        this.details = details;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAlertCode() { return alertCode; }
    public void setAlertCode(String alertCode) { this.alertCode = alertCode; }

    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }

    public String getConflictType() { return conflictType; }
    public void setConflictType(String conflictType) { this.conflictType = conflictType; }

    public String getTimeDetected() { return timeDetected; }
    public void setTimeDetected(String timeDetected) { this.timeDetected = timeDetected; }

    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }

    public String getDispatchStatus() { return dispatchStatus; }
    public void setDispatchStatus(String dispatchStatus) { this.dispatchStatus = dispatchStatus; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getStationCode() { return stationCode; }
    public void setStationCode(String stationCode) { this.stationCode = stationCode; }

    public int getTrackNumber() { return trackNumber; }
    public void setTrackNumber(int trackNumber) { this.trackNumber = trackNumber; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}

