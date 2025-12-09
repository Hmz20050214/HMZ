package com.example.parkingwebbackend.mapper;

import com.example.parking.model.ParkingSpot;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ParkingSpotMapper {
    List<ParkingSpot> getAllSpots();
    ParkingSpot getSpotById(Integer spotId);
    int updateSpotStatus(Integer spotId, String status);
    int insertSpot(ParkingSpot spot);
    int deleteSpot(Integer spotId);
}