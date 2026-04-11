package com.train.trainservice.dto;


import lombok.Data;

@Data
public class RouteRequest {

    private String stationName;
    private String stationCode;
    private int order;
    private String arrival;
    private String departure;
    private int distanceFromSource;
}