package com.smartrail.model;

/**
 * Operational train status indicators matching control room visual codes:
 * - Green: On time
 * - Amber: Delayed
 * - Red: Track conflict
 * - Blue: Moving
 * - Teal: Arrived (Destination reached)
 */
public enum TrainStatus {
    ON_TIME("On Time", "status-green", "#10B981", "Optimal Schedule"),
    DELAYED("Delayed", "status-amber", "#F59E0B", "Schedule Deviation"),
    CONFLICT("Track Conflict", "status-red", "#EF4444", "Interlocking Halt"),
    MOVING("Moving", "status-blue", "#3B82F6", "Active Line Transit"),
    HELD("Held at Signal", "status-red", "#EF4444", "Signal Interlocking Hold"),
    ARRIVED("Successfully Reached Destination", "status-teal", "#14B8A6", "Terminated at Platform");

    private final String label;
    private final String cssClass;
    private final String colorHex;
    private final String description;

    TrainStatus(String label, String cssClass, String colorHex, String description) {
        this.label = label;
        this.cssClass = cssClass;
        this.colorHex = colorHex;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getCssClass() {
        return cssClass;
    }

    public String getColorHex() {
        return colorHex;
    }

    public String getDescription() {
        return description;
    }

    public static TrainStatus fromString(String text) {
        if (text == null) return ON_TIME;
        for (TrainStatus status : TrainStatus.values()) {
            if (status.name().equalsIgnoreCase(text.trim()) || status.label.equalsIgnoreCase(text.trim())) {
                return status;
            }
        }
        return ON_TIME;
    }
}

