package com.example.parkingwebbackend.model;

import java.math.BigDecimal;
import java.util.Date;

public class ParkingRecord {
    private Integer recordId;
    private String plateNum;
    private Integer spotId;
    private Date entryTime;
    private Date exitTime;
    private BigDecimal payment;

    // Getter and Setter methods
    public Integer getRecordId() { return recordId; }
    public void setRecordId(Integer recordId) { this.recordId = recordId; }
    public String getPlateNum() { return plateNum; }
    public void setPlateNum(String plateNum) { this.plateNum = plateNum; }
    public Integer getSpotId() { return spotId; }
    public void setSpotId(Integer spotId) { this.spotId = spotId; }
    public Date getEntryTime() { return entryTime; }
    public void setEntryTime(Date entryTime) { this.entryTime = entryTime; }
    public Date getExitTime() { return exitTime; }
    public void setExitTime(Date exitTime) { this.exitTime = exitTime; }
    public BigDecimal getPayment() { return payment; }
    public void setPayment(BigDecimal payment) { this.payment = payment; }
}