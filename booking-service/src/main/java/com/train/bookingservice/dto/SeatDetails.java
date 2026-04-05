package com.train.bookingservice.dto;

import lombok.Data;

@Data
public class SeatDetails {

    private Long passengerId;
    private String coachNumber;
    private String seatNumber;

}