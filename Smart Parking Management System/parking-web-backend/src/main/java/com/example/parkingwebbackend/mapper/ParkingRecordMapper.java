package com.example.parkingwebbackend.mapper;

import com.example.parking.model.ParkingRecord;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ParkingRecordMapper {
    int insertRecord(ParkingRecord record);
    int updateRecord(ParkingRecord record);
    ParkingRecord getActiveRecordBySpotId(Integer spotId);
    ParkingRecord getRecordById(Integer recordId);
    List<ParkingRecord> getAllRecords();
}