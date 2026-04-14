package com.train.notificationservice.service;

import com.train.notificationservice.dto.BookingEvent;
import com.train.notificationservice.dto.NotificationRequest;
import com.train.notificationservice.dto.PaymentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class KafkaConsumerService {

    @Autowired
    private NotificationService notificationService;

    @KafkaListener(topics = "booking-topic", groupId = "train-group")
    public void consumeBookingEvent(String message) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            BookingEvent event = mapper.readValue(message, BookingEvent.class);
            NotificationRequest request = new NotificationRequest();
            request.setBookingId(event.getBookingId());
            request.setMessage(event.getMessage());
            request.setType(event.getType());
            request.setEmail(event.getEmail());

            notificationService.sendNotification(request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "payment-topic", groupId = "notification-group")
    public void consumePaymentEvent(String message) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            PaymentEvent event = mapper.readValue(message, PaymentEvent.class);

            NotificationRequest request = new NotificationRequest();
            request.setBookingId(event.getBookingId());
            request.setMessage(event.getMessage());
            request.setType("PAYMENT");
            request.setEmail(event.getEmail());

            notificationService.sendNotification(request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}