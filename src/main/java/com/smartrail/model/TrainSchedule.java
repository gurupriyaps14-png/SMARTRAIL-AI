package com.smartrail.model;

public class TrainSchedule {
    private int id;
    private int trainId;
    private String trainNumber;
    private String trainName;
    private int stationId;
    private String stationCode;
    private String stationName;
    private String scheduledArrival;
    private String scheduledDeparture;
    private int platformNumber;
    private int stopSequence;
    private int haltDurationMinutes;

    public TrainSchedule() {}

    public TrainSchedule(int id, int trainId, String trainNumber, String trainName,
                         int stationId, String stationCode, String stationName,
                         String scheduledArrival, String scheduledDeparture,
                         int platformNumber, int stopSequence, int haltDurationMinutes) {
        this.id = id;
        this.trainId = trainId;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.stationId = stationId;
        this.stationCode = stationCode;
        this.stationName = stationName;
        this.scheduledArrival = scheduledArrival;
        this.scheduledDeparture = scheduledDeparture;
        this.platformNumber = platformNumber;
        this.stopSequence = stopSequence;
        this.haltDurationMinutes = haltDurationMinutes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTrainId() { return trainId; }
    public void setTrainId(int trainId) { this.trainId = trainId; }

    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }

    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) { this.trainName = trainName; }

    public int getStationId() { return stationId; }
    public void setStationId(int stationId) { this.stationId = stationId; }

    public String getStationCode() { return stationCode; }
    public void setStationCode(String stationCode) { this.stationCode = stationCode; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getScheduledArrival() { return scheduledArrival; }
    public void setScheduledArrival(String scheduledArrival) { this.scheduledArrival = scheduledArrival; }

    public String getScheduledDeparture() { return scheduledDeparture; }
    public void setScheduledDeparture(String scheduledDeparture) { this.scheduledDeparture = scheduledDeparture; }

    public int getPlatformNumber() { return platformNumber; }
    public void setPlatformNumber(int platformNumber) { this.platformNumber = platformNumber; }

    public int getStopSequence() { return stopSequence; }
    public void setStopSequence(int stopSequence) { this.stopSequence = stopSequence; }

    public int getHaltDurationMinutes() { return haltDurationMinutes; }
    public void setHaltDurationMinutes(int haltDurationMinutes) { this.haltDurationMinutes = haltDurationMinutes; }
}

