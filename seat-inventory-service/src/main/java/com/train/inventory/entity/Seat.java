package com.train.inventory.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "SEAT")
@Data
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seat_seq_gen")
    @SequenceGenerator(name = "seat_seq_gen", sequenceName = "seat_seq", allocationSize = 1)
    private Long seatId;

    @Column(nullable = false)
    private Long coachId;

    @Column(nullable = false)
    private String seatNumber;
}