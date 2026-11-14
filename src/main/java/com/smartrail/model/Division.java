package com.smartrail.model;

import java.io.Serializable;

/**
 * Model representing a Railway Operational Division in Tamil Nadu.
 */
public class Division implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String code;            // MAS, SA, TPJ, MDU
    private String name;            // Chennai Division, Salem Division, etc.
    private String headquarters;    // Chennai, Salem, etc.
    private String zone;            // Southern Railway (SR)
    private int activeTrainsCount;
    private String description;

    public Division() {}

    public Division(int id, String code, String name, String headquarters, String zone, int activeTrainsCount, String description) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.headquarters = headquarters;
        this.zone = zone;
        this.activeTrainsCount = activeTrainsCount;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeadquarters() {
        return headquarters;
    }

    public void setHeadquarters(String headquarters) {
        this.headquarters = headquarters;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public int getActiveTrainsCount() {
        return activeTrainsCount;
    }

    public void setActiveTrainsCount(int activeTrainsCount) {
        this.activeTrainsCount = activeTrainsCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

