package com.train.paymentservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserClient {

    private final RestTemplate restTemplate;

    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getUserEmail(Long userId) {
        return restTemplate.getForObject(
                "http://localhost:8084/users/" + userId + "/email",
                String.class
        );
    }

    public String getUserEmailByBookingId(Long bookingId) {
        return restTemplate.getForObject(
                "http://localhost:8084/users/email/booking/" + bookingId,
                String.class
        );
    }
}