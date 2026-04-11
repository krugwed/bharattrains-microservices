package com.train.trainservice.dto;

import lombok.Data;

@Data
public class AddRouteRequest {

    private Long trainId;
    private Long stationId;
    private int order;
    private String arrival;
    private String departure;
}