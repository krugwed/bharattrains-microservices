package com.train.inventory.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "SEAT_ALLOCATION")
@Data
public class SeatAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long allocationId;

    @Column(nullable = false)
    private Long seatId;

    @Column(nullable = false)
    private Long trainId;

    @Column(nullable = false)
    private LocalDate journeyDate;

    @Column(nullable = false)
    private Integer fromStationOrder;

    @Column(nullable = false)
    private Integer toStationOrder;

    private Long bookingId;

    private String status; // BOOKED / RAC / CANCELLED
}