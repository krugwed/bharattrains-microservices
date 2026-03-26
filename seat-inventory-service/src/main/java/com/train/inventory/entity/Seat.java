package com.train.inventory.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "SEAT")
@Data
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seatId;

    @Column(nullable = false)
    private Long coachId;

    @Column(nullable = false)
    private String seatNumber;

    private String seatType;   // LOWER, MIDDLE, UPPER, SIDE_LOWER etc
}