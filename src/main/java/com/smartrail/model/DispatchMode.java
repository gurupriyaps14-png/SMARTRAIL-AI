package com.smartrail.model;

/**
 * Controller dispatch operating mode.
 * - AUTOMATIC: The AI system automatically applies track diversions subject to safety rules.
 * - MANUAL: The AI system recommends optimal diversions, but the human railway controller must approve.
 */
public enum DispatchMode {
    AUTOMATIC("Automatic Dispatch", "AI auto-executes safe diversions", "#10B981"),
    MANUAL("Manual Dispatch", "Controller approval required before execution", "#F59E0B");

    private final String label;
    private final String description;
    private final String colorHex;

    DispatchMode(String label, String description, String colorHex) {
        this.label = label;
        this.description = description;
        this.colorHex = colorHex;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getColorHex() {
        return colorHex;
    }

    public static DispatchMode fromString(String text) {
        if (text == null) return MANUAL;
        for (DispatchMode m : DispatchMode.values()) {
            if (m.name().equalsIgnoreCase(text.trim()) || m.label.equalsIgnoreCase(text.trim())) {
                return m;
            }
        }
        return MANUAL;
    }
}

