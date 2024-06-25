package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Metrics {
    private String cpu;
    private String memory;
    private String disk;
    private String status;

    // Default constructor
    public Metrics() {
    }

    // Constructor with all fields
    public Metrics(String cpu, String memory, String disk, String status) {
        this.cpu = cpu;
        this.memory = memory;
        this.disk = disk;
        this.status = status;
    }

    // Getters and setters
    @JsonProperty("cpu")
    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    @JsonProperty("memory")
    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    @JsonProperty("disk")
    public String getDisk() {
        return disk;
    }

    public void setDisk(String disk) {
        this.disk = disk;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

