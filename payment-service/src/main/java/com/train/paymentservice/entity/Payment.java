package com.train.paymentservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Payment {

    @Id
    @GeneratedValue
    private Long paymentId;

    private Long bookingId;

    private Double amount;

    private String status; // SUCCESS / FAILED

    private String paymentMethod; // UPI / CARD

    private String transactionId;

    private LocalDateTime createdAt;
}