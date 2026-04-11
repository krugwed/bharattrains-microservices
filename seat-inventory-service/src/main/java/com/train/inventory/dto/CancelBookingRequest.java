package com.train.inventory.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CancelBookingRequest {
    private Long bookingId;
    private Long trainId;
    private LocalDate date;

    // getters and setters
}
