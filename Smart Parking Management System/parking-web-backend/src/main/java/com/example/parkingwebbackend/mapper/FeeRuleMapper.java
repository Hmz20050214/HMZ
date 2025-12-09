package com.example.parkingwebbackend.mapper;

import com.example.parking.model.FeeRule;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FeeRuleMapper {
    FeeRule getLatestRule();
    int insertRule(FeeRule rule);
    int updateRule(FeeRule rule);
}