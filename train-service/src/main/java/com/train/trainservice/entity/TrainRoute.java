package com.train.trainservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;

@Entity
@Table(name = "train_route")
@Data
public class TrainRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long trainId;

    private Long stationId;

    private int stopOrder; // used in seat allocation

    private LocalTime arrivalTime;
    private LocalTime departureTime;
    private Long distanceFromSource;
}