package com.train.inventory.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SeatBookingRequest  {
    private Long trainId;
    private LocalDate journeyDate;
    private int fromStation;
    private int toStation;
    private Long bookingId;
    private Long passengerId;

    // getters and setters
}
