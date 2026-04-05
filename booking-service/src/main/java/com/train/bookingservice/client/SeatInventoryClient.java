package com.train.bookingservice.client;

import com.train.bookingservice.dto.SeatDetails;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

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
                           Long bookingId,
                           Long passengerId) {

        String url = "http://localhost:8080/seats/book";

        return restTemplate.postForObject(
                url +
                        "?trainId=" + trainId +
                        "&date=" + date +
                        "&from=" + from +
                        "&to=" + to +
                        "&bookingId=" + bookingId +
                        "&passengerId=" + passengerId,
                null,
                String.class
        );
    }

    public void cancelBooking(Long bookingId,
                              Long trainId,
                              LocalDate date) {

        String url = "http://localhost:8080/seats/cancel";

        restTemplate.postForObject(
                url +
                        "?bookingId=" + bookingId +
                        "&trainId=" + trainId +
                        "&date=" + date,
                null,
                String.class
        );
    }

    public void cancelPassenger(Long passengerId,
                                Long trainId,
                                LocalDate date) {

        String url = "http://localhost:8080/seats/cancel/passenger";

        restTemplate.postForObject(
                url +
                        "?passengerId=" + passengerId +
                        "&trainId=" + trainId +
                        "&date=" + date,
                null,
                String.class
        );
    }

    public List<SeatDetails> getSeatDetails(Long bookingId) {

        ResponseEntity<List<SeatDetails>> response =
                restTemplate.exchange(
                        "http://seat-inventory-service/seats/" + bookingId,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<SeatDetails>>() {}
                );

        return response.getBody();
    }
}