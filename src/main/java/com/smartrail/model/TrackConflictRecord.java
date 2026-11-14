package com.smartrail.model;

public class TrackConflictRecord {
    private int id;
    private String conflictCode;
    private String stationCode;
    private int trackNumber;
    private String trainPrimary;
    private String trainSecondary;
    private String conflictType;
    private String severity;
    private String priorityWinner;
    private String decisionReason;
    private String detectedTime;
    private String status;

    public TrackConflictRecord() {}

    public TrackConflictRecord(int id, String conflictCode, String stationCode, int trackNumber,
                               String trainPrimary, String trainSecondary, String conflictType,
                               String severity, String priorityWinner, String decisionReason,
                               String detectedTime, String status) {
        this.id = id;
        this.conflictCode = conflictCode;
        this.stationCode = stationCode;
        this.trackNumber = trackNumber;
        this.trainPrimary = trainPrimary;
        this.trainSecondary = trainSecondary;
        this.conflictType = conflictType;
        this.severity = severity;
        this.priorityWinner = priorityWinner;
        this.decisionReason = decisionReason;
        this.detectedTime = detectedTime;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getConflictCode() { return conflictCode; }
    public void setConflictCode(String conflictCode) { this.conflictCode = conflictCode; }

    public String getStationCode() { return stationCode; }
    public void setStationCode(String stationCode) { this.stationCode = stationCode; }

    public int getTrackNumber() { return trackNumber; }
    public void setTrackNumber(int trackNumber) { this.trackNumber = trackNumber; }

    public String getTrainPrimary() { return trainPrimary; }
    public void setTrainPrimary(String trainPrimary) { this.trainPrimary = trainPrimary; }

    public String getTrainSecondary() { return trainSecondary; }
    public void setTrainSecondary(String trainSecondary) { this.trainSecondary = trainSecondary; }

    public String getConflictType() { return conflictType; }
    public void setConflictType(String conflictType) { this.conflictType = conflictType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getPriorityWinner() { return priorityWinner; }
    public void setPriorityWinner(String priorityWinner) { this.priorityWinner = priorityWinner; }

    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }

    public String getDetectedTime() { return detectedTime; }
    public void setDetectedTime(String detectedTime) { this.detectedTime = detectedTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

