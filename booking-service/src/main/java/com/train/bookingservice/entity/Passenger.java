package com.train.bookingservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "passenger")
@Data
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long passengerId;

    private Long bookingId;

    private String name;

    private int age;

    private String gender;

    private String status; // CONFIRMED / RAC / WL

    private Double fare;
}