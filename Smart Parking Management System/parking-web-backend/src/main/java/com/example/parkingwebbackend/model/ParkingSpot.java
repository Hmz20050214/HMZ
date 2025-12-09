package com.example.parkingwebbackend.model;

public class ParkingSpot {
    private Integer spotId;
    private String spotNumber;
    private String status; // FREE, OCCUPIED, RESERVED
    private Integer floor;

    // Getter and Setter methods
    public Integer getSpotId() { return spotId; }
    public void setSpotId(Integer spotId) { this.spotId = spotId; }
    public String getSpotNumber() { return spotNumber; }
    public void setSpotNumber(String spotNumber) { this.spotNumber = spotNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }
}