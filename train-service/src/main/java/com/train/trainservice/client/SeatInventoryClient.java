package com.train.trainservice.client;

import com.train.trainservice.dto.AvailabilityRequest;
import com.train.trainservice.dto.SeatSetupRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class SeatInventoryClient {

    private final RestTemplate restTemplate;

    public SeatInventoryClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<Long, Integer> getBulkAvailability(
            List<AvailabilityRequest> requests) {

        String url = "http://localhost:8084/seats/availability/bulk";

        ResponseEntity<Map<Long, Integer>> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        new HttpEntity<>(requests),
                        new ParameterizedTypeReference<>() {}
                );

        return response.getBody();
    }

    public void createSeats(SeatSetupRequest request) {
        restTemplate.postForObject(
                "http://localhost:8084/seats/setup",
                request,
                String.class
        );
    }
}