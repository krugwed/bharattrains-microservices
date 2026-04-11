package com.train.trainservice.dto;

import lombok.Data;

@Data
public class StationOrderRequest {

    private Long trainId;
    private String stationCode;
}