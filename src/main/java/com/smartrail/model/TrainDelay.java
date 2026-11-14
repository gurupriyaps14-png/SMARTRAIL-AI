package com.smartrail.model;

public class TrainDelay {
    private int id;
    private String trainNumber;
    private String stationCode;
    private int delayMinutes;
    private String delayReason;
    private String recordedAt;

    public TrainDelay() {}

    public TrainDelay(int id, String trainNumber, String stationCode, int delayMinutes, String delayReason, String recordedAt) {
        this.id = id;
        this.trainNumber = trainNumber;
        this.stationCode = stationCode;
        this.delayMinutes = delayMinutes;
        this.delayReason = delayReason;
        this.recordedAt = recordedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }

    public String getStationCode() { return stationCode; }
    public void setStationCode(String stationCode) { this.stationCode = stationCode; }

    public int getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(int delayMinutes) { this.delayMinutes = delayMinutes; }

    public String getDelayReason() { return delayReason; }
    public void setDelayReason(String delayReason) { this.delayReason = delayReason; }

    public String getRecordedAt() { return recordedAt; }
    public void setRecordedAt(String recordedAt) { this.recordedAt = recordedAt; }
}

