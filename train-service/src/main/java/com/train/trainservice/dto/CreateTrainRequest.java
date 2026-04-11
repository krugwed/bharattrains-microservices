package com.train.trainservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateTrainRequest {

    private String trainName;
    private List<RouteRequest> routes;
}