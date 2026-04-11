package com.train.inventory.dto;

import lombok.Data;

@Data
public class AvailabilityCheckRequest {

    private Long trainId;
    private String journeyDate;
    private int fromStation;
    private int toStation;

    // getters and setters
}