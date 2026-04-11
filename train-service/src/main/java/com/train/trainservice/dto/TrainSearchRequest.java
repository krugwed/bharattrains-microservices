package com.train.trainservice.dto;

import lombok.Data;

@Data
public class TrainSearchRequest {

    private String source;
    private String destination;
    private String date;
}