package com.example.parkingwebbackend.service;

import com.example.parking.model.ParkingSpot;
import com.example.parking.model.ParkingRecord;
import java.math.BigDecimal;
import java.util.List;

public interface ParkingService {
    List<ParkingSpot> getAllSpots();
    boolean parkIn(String plateNum, Integer spotId);
    BigDecimal parkOut(Integer spotId);
    List<ParkingRecord> getParkingRecords();
    ParkingRecord getActiveRecordBySpotId(Integer spotId);
}