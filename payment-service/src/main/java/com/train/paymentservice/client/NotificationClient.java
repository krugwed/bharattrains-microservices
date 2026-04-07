package com.train.paymentservice.client;

import com.train.paymentservice.dto.NotificationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NotificationClient {

    private final RestTemplate restTemplate;

    public NotificationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendNotification(NotificationRequest notificationRequest) {
        restTemplate.postForObject(
                "http://localhost:8085/notifications/send",
                notificationRequest,
                String.class
        );
    }
}