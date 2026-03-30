package com.train.bookingservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@Component
public class SeatInventoryClient {

    private final RestTemplate restTemplate;

    public SeatInventoryClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String bookSeat(Long trainId,
                           LocalDate date,
                           int from,
                           int to,
                           Long bookingId) {

        String url = "http://localhost:8080/seats/book";

        return restTemplate.postForObject(
                url +
                        "?trainId=" + trainId +
                        "&date=" + date +
                        "&from=" + from +
                        "&to=" + to +
                        "&bookingId=" + bookingId,
                null,
                String.class
        );
    }
}