package com.train.inventory.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AvailabilityRequest {

    private Long trainId;
    private LocalDate journeyDate;
    private int from;
    private int to;
}