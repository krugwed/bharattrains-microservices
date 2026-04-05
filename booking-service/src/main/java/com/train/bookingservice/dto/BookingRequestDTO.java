package com.train.bookingservice.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BookingRequestDTO {

    private Long userId;

    private Long trainId;

    private LocalDate journeyDate;

    private int fromStation;

    private int toStation;

    private List<PassengerDTO> passengers;

    private String coachType; // SLEEPER / AC3 / AC2 etc
}