package com.train.bookingservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TrainClient {

    private final RestTemplate restTemplate;

    public TrainClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Double getFare(Long trainId,
                          String source,
                          String destination,
                          String coachType,
                          String date) {

        String url = "http://localhost:8083/train/fare"
                + "?trainId=" + trainId
                + "&source=" + source
                + "&destination=" + destination
                + "&coachType=" + coachType
                + "&date=" + date;

        return restTemplate.getForObject(url, Double.class);
    }
}
