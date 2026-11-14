package com.smartrail.model;

public class DiversionRecord {
    private int id;
    private String conflictCode;
    private String trainNumber;
    private int divertedFromTrack;
    private int divertedToTrack;
    private String stationCode;
    private String actionType;
    private boolean controllerApproved;
    private String reason;
    private String diversionTime;

    public DiversionRecord() {}

    public DiversionRecord(int id, String conflictCode, String trainNumber,
                           int divertedFromTrack, int divertedToTrack, String stationCode,
                           String actionType, boolean controllerApproved, String reason,
                           String diversionTime) {
        this.id = id;
        this.conflictCode = conflictCode;
        this.trainNumber = trainNumber;
        this.divertedFromTrack = divertedFromTrack;
        this.divertedToTrack = divertedToTrack;
        this.stationCode = stationCode;
        this.actionType = actionType;
        this.controllerApproved = controllerApproved;
        this.reason = reason;
        this.diversionTime = diversionTime;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getConflictCode() { return conflictCode; }
    public void setConflictCode(String conflictCode) { this.conflictCode = conflictCode; }

    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }

    public int getDivertedFromTrack() { return divertedFromTrack; }
    public void setDivertedFromTrack(int divertedFromTrack) { this.divertedFromTrack = divertedFromTrack; }

    public int getDivertedToTrack() { return divertedToTrack; }
    public void setDivertedToTrack(int divertedToTrack) { this.divertedToTrack = divertedToTrack; }

    public String getStationCode() { return stationCode; }
    public void setStationCode(String stationCode) { this.stationCode = stationCode; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public boolean isControllerApproved() { return controllerApproved; }
    public void setControllerApproved(boolean controllerApproved) { this.controllerApproved = controllerApproved; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDiversionTime() { return diversionTime; }
    public void setDiversionTime(String diversionTime) { this.diversionTime = diversionTime; }
}

