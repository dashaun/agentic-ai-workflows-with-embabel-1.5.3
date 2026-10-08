package com.embabel.demo.domain;

public enum IncidentType {
    OUT_OF_MEMORY("OutOfMemory"),
    HIGH_LATENCY("High Latency");

    private final String displayName;

    IncidentType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
