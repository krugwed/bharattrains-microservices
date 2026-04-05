package com.train.bookingservice.entity;

import lombok.Data;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "booking")
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    private String pnr;

    private Long trainId;

    private LocalDate journeyDate;

    private int fromStation;

    private int toStation;

    private String status;

    private Double totalFare;
}