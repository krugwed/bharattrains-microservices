package com.train.paymentservice.dto;

import lombok.Data;

@Data
public class PaymentRequest {

    private Long bookingId;
    private Double amount;
    private String paymentMethod;
}