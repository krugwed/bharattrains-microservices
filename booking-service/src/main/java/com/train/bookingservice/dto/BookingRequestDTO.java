package com.train.bookingservice.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BookingRequestDTO {

    private Long trainId;

    private LocalDate journeyDate;

    private int fromStation;

    private int toStation;

    private List<PassengerDTO> passengers;
}