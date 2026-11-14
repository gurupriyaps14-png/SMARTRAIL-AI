package com.smartrail.model;

/**
 * Train category enum matching railway operational classifications.
 */
public enum TrainCategory {
    PASSENGER("Passenger", "bg-teal-subtle text-teal border-teal", "#06B6D4"),
    EXPRESS("Express", "bg-blue-subtle text-blue border-blue", "#3B82F6"),
    GOODS("Goods", "bg-amber-subtle text-amber border-amber", "#F59E0B"),
    FREIGHT("Freight", "bg-purple-subtle text-purple border-purple", "#A855F7");

    private final String displayName;
    private final String cssClass;
    private final String colorHex;

    TrainCategory(String displayName, String cssClass, String colorHex) {
        this.displayName = displayName;
        this.cssClass = cssClass;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCssClass() {
        return cssClass;
    }

    public String getColorHex() {
        return colorHex;
    }

    public static TrainCategory fromString(String text) {
        if (text == null) return EXPRESS;
        for (TrainCategory category : TrainCategory.values()) {
            if (category.name().equalsIgnoreCase(text.trim()) || category.displayName.equalsIgnoreCase(text.trim())) {
                return category;
            }
        }
        return EXPRESS;
    }
}

