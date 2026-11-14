package com.smartrail.model;

public class TrainMovement {
    private int id;
    private String trainNumber;
    private String stationCode;
    private int trackNumber;
    private int speedKmh;
    private double progressPercent;
    private String movementStatus;
    private String timestamp;
    private String notes;

    public TrainMovement() {}

    public TrainMovement(int id, String trainNumber, String stationCode, int trackNumber,
                         int speedKmh, double progressPercent, String movementStatus,
                         String timestamp, String notes) {
        this.id = id;
        this.trainNumber = trainNumber;
        this.stationCode = stationCode;
        this.trackNumber = trackNumber;
        this.speedKmh = speedKmh;
        this.progressPercent = progressPercent;
        this.movementStatus = movementStatus;
        this.timestamp = timestamp;
        this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }

    public String getStationCode() { return stationCode; }
    public void setStationCode(String stationCode) { this.stationCode = stationCode; }

    public int getTrackNumber() { return trackNumber; }
    public void setTrackNumber(int trackNumber) { this.trackNumber = trackNumber; }

    public int getSpeedKmh() { return speedKmh; }
    public void setSpeedKmh(int speedKmh) { this.speedKmh = speedKmh; }

    public double getProgressPercent() { return progressPercent; }
    public void setProgressPercent(double progressPercent) { this.progressPercent = progressPercent; }

    public String getMovementStatus() { return movementStatus; }
    public void setMovementStatus(String movementStatus) { this.movementStatus = movementStatus; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

