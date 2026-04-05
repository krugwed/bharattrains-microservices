package com.train.paymentservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BookingClient {

    private final RestTemplate restTemplate;

    public BookingClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void updatePaymentStatus(Long bookingId, String status) {

        restTemplate.put(
                "http://localhost:8081/bookings/" + bookingId + "/status?value=" + status,
                null
        );
    }
}