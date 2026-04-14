package com.train.paymentservice.service;

import com.train.paymentservice.client.BookingClient;
import com.train.paymentservice.client.NotificationClient;
import com.train.paymentservice.client.UserClient;
import com.train.paymentservice.dto.NotificationRequest;
import com.train.paymentservice.dto.PaymentEvent;
import com.train.paymentservice.dto.PaymentRequest;
import com.train.paymentservice.entity.Payment;
import com.train.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingClient bookingClient;
    private final NotificationClient notificationClient;
    private final UserClient userClient;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private ObjectMapper objectMapper;

    public PaymentService(PaymentRepository paymentRepository,
                          BookingClient bookingClient,
                          NotificationClient notificationClient,
                          UserClient userClient) {
        this.paymentRepository = paymentRepository;
        this.bookingClient = bookingClient;
        this.notificationClient = notificationClient;
        this.userClient = userClient;
    }

    public String processPayment(PaymentRequest request) {

        // simulate payment success
        boolean success = true;

        Payment payment = new Payment();
        payment.setBookingId(request.getBookingId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTransactionId("TXN" + System.currentTimeMillis());
        payment.setCreatedAt(LocalDateTime.now());
        String emailID = userClient.getUserEmailByBookingId(request.getBookingId());
        if (success) {
            payment.setStatus("SUCCESS");

            // update booking
            bookingClient.updatePaymentStatus(
                    request.getBookingId(),
                    "PAID"
            );

            // send notification
            PaymentEvent event = new PaymentEvent();
            event.setBookingId(payment.getBookingId());
            event.setEmail(emailID);
            event.setAmount(payment.getAmount());
            event.setStatus("SUCCESS");
            event.setMessage("Payment of Rs." + payment.getAmount() + " successful for booking " + payment.getBookingId());

            String eventJson = objectMapper.writeValueAsString(event);

            kafkaProducerService.sendPaymentEvent(eventJson);

//            NotificationRequest notificationRequest = new NotificationRequest();
//            notificationRequest.setBookingId(request.getBookingId());
//            notificationRequest.setMessage("Your payment was successful. Booking ID: " + request.getBookingId());
//            notificationRequest.setType("EMAIL");
//            notificationRequest.setEmail(emailID);
//            notificationClient.sendNotification(notificationRequest);

        } else {
            payment.setStatus("FAILED");

            PaymentEvent event = new PaymentEvent();
            event.setBookingId(payment.getBookingId());
            event.setEmail(emailID);
            event.setAmount(payment.getAmount());
            event.setStatus("SUCCESS");
            event.setMessage("Payment of Rs." + payment.getAmount() + " successful for booking " + payment.getBookingId());

            String eventJson = objectMapper.writeValueAsString(event);

            kafkaProducerService.sendPaymentEvent(eventJson);

            bookingClient.updatePaymentStatus(
                    request.getBookingId(),
                    "PAYMENT_FAILED"
            );
        }

        paymentRepository.save(payment);

        return payment.getStatus();
    }
}