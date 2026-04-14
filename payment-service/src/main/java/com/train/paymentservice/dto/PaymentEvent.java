package com.train.paymentservice.dto;

import lombok.Data;

@Data
public class PaymentEvent {
    private Long bookingId;
    private String email;
    private Double amount;
    private String status;
    private String message;
}
