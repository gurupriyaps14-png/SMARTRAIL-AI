package com.smartrail.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing an identifiable railway track section / circuit block.
 * Manages occupancy, entry/exit points, interlocking signal aspects, and connected sections.
 */
public class TrackSection implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sectionId;              // e.g. SEC_T1_WEST, SEC_T1_JUNCTION, SEC_TO_1_3
    private int trackNumber;               // 1 to 5, or 13 for connecting crossover 1-3
    private String name;                   // Human readable name
    private double startProgress;          // Entry progress boundary (0.0 - 100.0)
    private double endProgress;            // Exit progress boundary (0.0 - 100.0)
    private String status;                 // CLEAR, OCCUPIED, RESERVED
    private String occupyingTrainNumber;   // Train ID occupying this section, or null
    private String occupyingTrainDirection;// UP, DOWN, or null
    private String signalAspect;           // GREEN, AMBER, RED
    private int speedLimitKmh;             // Speed limit in section
    private boolean isTurnout;             // True if this is a crossover switch/side track
    private List<String> connectedSectionIds;

    public TrackSection() {
        this.status = "CLEAR";
        this.signalAspect = "GREEN";
        this.speedLimitKmh = 100;
        this.connectedSectionIds = new ArrayList<>();
    }

    public TrackSection(String sectionId, int trackNumber, String name,
                        double startProgress, double endProgress,
                        int speedLimitKmh, boolean isTurnout) {
        this.sectionId = sectionId;
        this.trackNumber = trackNumber;
        this.name = name;
        this.startProgress = startProgress;
        this.endProgress = endProgress;
        this.speedLimitKmh = speedLimitKmh;
        this.isTurnout = isTurnout;
        this.status = "CLEAR";
        this.signalAspect = "GREEN";
        this.occupyingTrainNumber = null;
        this.occupyingTrainDirection = null;
        this.connectedSectionIds = new ArrayList<>();
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getStartProgress() {
        return startProgress;
    }

    public void setStartProgress(double startProgress) {
        this.startProgress = startProgress;
    }

    public double getEndProgress() {
        return endProgress;
    }

    public void setEndProgress(double endProgress) {
        this.endProgress = endProgress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOccupyingTrainNumber() {
        return occupyingTrainNumber;
    }

    public void setOccupyingTrainNumber(String occupyingTrainNumber) {
        this.occupyingTrainNumber = occupyingTrainNumber;
    }

    public String getOccupyingTrainDirection() {
        return occupyingTrainDirection;
    }

    public void setOccupyingTrainDirection(String occupyingTrainDirection) {
        this.occupyingTrainDirection = occupyingTrainDirection;
    }

    public String getSignalAspect() {
        return signalAspect;
    }

    public void setSignalAspect(String signalAspect) {
        this.signalAspect = signalAspect;
    }

    public int getSpeedLimitKmh() {
        return speedLimitKmh;
    }

    public void setSpeedLimitKmh(int speedLimitKmh) {
        this.speedLimitKmh = speedLimitKmh;
    }

    public boolean isTurnout() {
        return isTurnout;
    }

    public void setTurnout(boolean turnout) {
        isTurnout = turnout;
    }

    public List<String> getConnectedSectionIds() {
        return connectedSectionIds;
    }

    public void setConnectedSectionIds(List<String> connectedSectionIds) {
        this.connectedSectionIds = connectedSectionIds;
    }

    public void addConnectedSection(String id) {
        if (!this.connectedSectionIds.contains(id)) {
            this.connectedSectionIds.add(id);
        }
    }

    public boolean containsProgress(double progress) {
        return progress >= startProgress && progress <= endProgress;
    }
}
