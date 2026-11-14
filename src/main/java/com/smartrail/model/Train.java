package com.smartrail.model;

import java.io.Serializable;

/**
 * Model representing an active or scheduled Train moving across Tamil Nadu divisions.
 */
public class Train implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String trainNumber;         // e.g. 12637
    private String trainName;           // Pandian Superfast Express
    private TrainCategory category;     // PASSENGER, EXPRESS, GOODS, FREIGHT
    private int originStationId;
    private String originStationName;
    private int destinationStationId;
    private String destinationStationName;
    private int currentStationId;
    private String currentStationName;
    private String nextStationName;
    private int currentTrackNumber;     // Track 1, 2, 3, 4, 5
    private String direction;           // UP, DOWN
    private TrainStatus status;         // ON_TIME, DELAYED, CONFLICT, MOVING, ARRIVED
    private String eta;                 // 22:45
    private String etd;                 // 22:50
    private int delayMinutes;           // 0, 14, etc.
    private int speedKmh;               // 88
    private double progressPercent;     // 0.0 to 100.0%
    private String timeSlot;            // ALL, MORNING, MIDDAY, EVENING, NIGHT
    private boolean isSimulated;        // True initially (clearly distinguished)

    // Interlocking, Section Occupancy, Spacing & Turnout Geometry Kinematics
    private String currentSectionId;
    private String signalAspect = "GREEN";     // GREEN, AMBER, RED
    private boolean isDiverting = false;
    private double diversionProgress = 0.0;
    private String diversionSegment;           // APPROACH_TRACK_1, CROSSOVER_TURNOUT, MERGE_TRACK_3
    private int fromTrackNumber = 1;
    private int toTrackNumber = 3;
    private double posX = 0.0;
    private double posY = 0.0;
    private double rotationDeg = 0.0;
    private String spacingStatus = "NOMINAL";  // NOMINAL, CAUTION_SLOW, STANDSTILL_HELD
    private String leadTrainNumber;
    private double separationDistancePercent = 100.0;
    private double dwellRemaining = 0.0;

    public Train() {
        this.category = TrainCategory.EXPRESS;
        this.status = TrainStatus.ON_TIME;
        this.isSimulated = true;
    }

    public Train(int id, String trainNumber, String trainName, TrainCategory category,
                 int originStationId, String originStationName,
                 int destinationStationId, String destinationStationName,
                 int currentStationId, String currentStationName, String nextStationName,
                 int currentTrackNumber, String direction, TrainStatus status,
                 String eta, String etd, int delayMinutes, int speedKmh,
                 double progressPercent, String timeSlot, boolean isSimulated) {
        this.id = id;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.category = category;
        this.originStationId = originStationId;
        this.originStationName = originStationName;
        this.destinationStationId = destinationStationId;
        this.destinationStationName = destinationStationName;
        this.currentStationId = currentStationId;
        this.currentStationName = currentStationName;
        this.nextStationName = nextStationName;
        this.currentTrackNumber = currentTrackNumber;
        this.direction = direction;
        this.status = status;
        this.eta = eta;
        this.etd = etd;
        this.delayMinutes = delayMinutes;
        this.speedKmh = speedKmh;
        this.progressPercent = progressPercent;
        this.timeSlot = timeSlot;
        this.isSimulated = isSimulated;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public TrainCategory getCategory() {
        return category;
    }

    public void setCategory(TrainCategory category) {
        this.category = category;
    }

    public int getOriginStationId() {
        return originStationId;
    }

    public void setOriginStationId(int originStationId) {
        this.originStationId = originStationId;
    }

    public String getOriginStationName() {
        return originStationName;
    }

    public void setOriginStationName(String originStationName) {
        this.originStationName = originStationName;
    }

    public int getDestinationStationId() {
        return destinationStationId;
    }

    public void setDestinationStationId(int destinationStationId) {
        this.destinationStationId = destinationStationId;
    }

    public String getDestinationStationName() {
        return destinationStationName;
    }

    public void setDestinationStationName(String destinationStationName) {
        this.destinationStationName = destinationStationName;
    }

    public int getCurrentStationId() {
        return currentStationId;
    }

    public void setCurrentStationId(int currentStationId) {
        this.currentStationId = currentStationId;
    }

    public String getCurrentStationName() {
        return currentStationName;
    }

    public void setCurrentStationName(String currentStationName) {
        this.currentStationName = currentStationName;
    }

    public String getNextStationName() {
        return nextStationName;
    }

    public void setNextStationName(String nextStationName) {
        this.nextStationName = nextStationName;
    }

    public int getCurrentTrackNumber() {
        return currentTrackNumber;
    }

    public void setCurrentTrackNumber(int currentTrackNumber) {
        this.currentTrackNumber = currentTrackNumber;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public TrainStatus getStatus() {
        return status;
    }

    public void setStatus(TrainStatus status) {
        this.status = status;
    }

    public String getEta() {
        return eta;
    }

    public void setEta(String eta) {
        this.eta = eta;
    }

    public String getEtd() {
        return etd;
    }

    public void setEtd(String etd) {
        this.etd = etd;
    }

    public int getDelayMinutes() {
        return delayMinutes;
    }

    public void setDelayMinutes(int delayMinutes) {
        this.delayMinutes = delayMinutes;
    }

    public int getSpeedKmh() {
        return speedKmh;
    }

    public void setSpeedKmh(int speedKmh) {
        this.speedKmh = speedKmh;
    }

    public double getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(double progressPercent) {
        this.progressPercent = progressPercent;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public boolean isSimulated() {
        return isSimulated;
    }

    public void setSimulated(boolean simulated) {
        isSimulated = simulated;
    }

    public String getCurrentSectionId() {
        return currentSectionId;
    }

    public void setCurrentSectionId(String currentSectionId) {
        this.currentSectionId = currentSectionId;
    }

    public String getSignalAspect() {
        return signalAspect;
    }

    public void setSignalAspect(String signalAspect) {
        this.signalAspect = signalAspect;
    }

    public boolean isDiverting() {
        return isDiverting;
    }

    public void setDiverting(boolean diverting) {
        isDiverting = diverting;
    }

    public double getDiversionProgress() {
        return diversionProgress;
    }

    public void setDiversionProgress(double diversionProgress) {
        this.diversionProgress = diversionProgress;
    }

    public String getDiversionSegment() {
        return diversionSegment;
    }

    public void setDiversionSegment(String diversionSegment) {
        this.diversionSegment = diversionSegment;
    }

    public int getFromTrackNumber() {
        return fromTrackNumber;
    }

    public void setFromTrackNumber(int fromTrackNumber) {
        this.fromTrackNumber = fromTrackNumber;
    }

    public int getToTrackNumber() {
        return toTrackNumber;
    }

    public void setToTrackNumber(int toTrackNumber) {
        this.toTrackNumber = toTrackNumber;
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public double getRotationDeg() {
        return rotationDeg;
    }

    public void setRotationDeg(double rotationDeg) {
        this.rotationDeg = rotationDeg;
    }

    public String getSpacingStatus() {
        return spacingStatus;
    }

    public void setSpacingStatus(String spacingStatus) {
        this.spacingStatus = spacingStatus;
    }

    public String getLeadTrainNumber() {
        return leadTrainNumber;
    }

    public void setLeadTrainNumber(String leadTrainNumber) {
        this.leadTrainNumber = leadTrainNumber;
    }

    public double getSeparationDistancePercent() {
        return separationDistancePercent;
    }

    public void setSeparationDistancePercent(double separationDistancePercent) {
        this.separationDistancePercent = separationDistancePercent;
    }

    public double getDwellRemaining() {
        return dwellRemaining;
    }

    public void setDwellRemaining(double dwellRemaining) {
        this.dwellRemaining = dwellRemaining;
    }
}

