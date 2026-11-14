package com.smartrail.model;

import java.io.Serializable;

/**
 * Model representing a physical railway turnout / switch point.
 * Controls routing between main line tracks and connecting crossover side tracks.
 */
public class JunctionSwitch implements Serializable {
    private static final long serialVersionUID = 1L;

    private String junctionId;          // e.g. JNC_T1_WEST
    private String name;                // e.g. West Interlocking Switch 1A
    private int sourceTrackNumber;      // e.g. 1
    private int destinationTrackNumber; // e.g. 3
    private String normalSectionId;     // e.g. SEC_T1_MID (Straight route)
    private String reverseSectionId;    // e.g. SEC_TO_1_3 (Turnout side track)
    private String alignment;           // NORMAL, REVERSE
    private boolean isLocked;           // True when reserved or train is traversing
    private String reservedForTrain;    // Train ID holding the lock
    private double positionX;           // Pixel X on track display (e.g. 260.0)
    private double positionY;           // Pixel Y on track display (e.g. 90.0)

    public JunctionSwitch() {
        this.alignment = "NORMAL";
        this.isLocked = false;
    }

    public JunctionSwitch(String junctionId, String name, int sourceTrackNumber, int destinationTrackNumber,
                          String normalSectionId, String reverseSectionId,
                          double positionX, double positionY) {
        this.junctionId = junctionId;
        this.name = name;
        this.sourceTrackNumber = sourceTrackNumber;
        this.destinationTrackNumber = destinationTrackNumber;
        this.normalSectionId = normalSectionId;
        this.reverseSectionId = reverseSectionId;
        this.positionX = positionX;
        this.positionY = positionY;
        this.alignment = "NORMAL";
        this.isLocked = false;
        this.reservedForTrain = null;
    }

    public String getJunctionId() {
        return junctionId;
    }

    public void setJunctionId(String junctionId) {
        this.junctionId = junctionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSourceTrackNumber() {
        return sourceTrackNumber;
    }

    public void setSourceTrackNumber(int sourceTrackNumber) {
        this.sourceTrackNumber = sourceTrackNumber;
    }

    public int getDestinationTrackNumber() {
        return destinationTrackNumber;
    }

    public void setDestinationTrackNumber(int destinationTrackNumber) {
        this.destinationTrackNumber = destinationTrackNumber;
    }

    public String getNormalSectionId() {
        return normalSectionId;
    }

    public void setNormalSectionId(String normalSectionId) {
        this.normalSectionId = normalSectionId;
    }

    public String getReverseSectionId() {
        return reverseSectionId;
    }

    public void setReverseSectionId(String reverseSectionId) {
        this.reverseSectionId = reverseSectionId;
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public String getReservedForTrain() {
        return reservedForTrain;
    }

    public void setReservedForTrain(String reservedForTrain) {
        this.reservedForTrain = reservedForTrain;
    }

    public double getPositionX() {
        return positionX;
    }

    public void setPositionX(double positionX) {
        this.positionX = positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public void setPositionY(double positionY) {
        this.positionY = positionY;
    }
}
