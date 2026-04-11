package com.train.trainservice.dto;

import lombok.Data;

@Data
public class SeatSetupRequest {

    private Long trainId;
    private int sleeperCoaches;
    private int ac3Coaches;
    private int ac2Coaches;
    private int seatsPerCoach;
}
