package com.example.parkingwebbackend.model;

import java.math.BigDecimal;

public class FeeRule {
    private Integer ruleId;
    private BigDecimal basePrice;
    private Integer freeMinutes;
    private BigDecimal dailyCap;

    // Getter and Setter methods
    public Integer getRuleId() { return ruleId; }
    public void setRuleId(Integer ruleId) { this.ruleId = ruleId; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public Integer getFreeMinutes() { return freeMinutes; }
    public void setFreeMinutes(Integer freeMinutes) { this.freeMinutes = freeMinutes; }
    public BigDecimal getDailyCap() { return dailyCap; }
    public void setDailyCap(BigDecimal dailyCap) { this.dailyCap = dailyCap; }
}