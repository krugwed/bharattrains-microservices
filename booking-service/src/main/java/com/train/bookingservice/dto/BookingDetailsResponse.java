package com.train.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDetailsResponse {

    private String pnr;

    private Long trainId;

    private LocalDate journeyDate;

    private int fromStation;

    private int toStation;

    private String bookingStatus;

    private List<PassengerDetails> passengers;
}