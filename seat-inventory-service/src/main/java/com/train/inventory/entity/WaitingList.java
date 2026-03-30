package com.train.inventory.entity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "WAITING_LIST")
@Data
public class WaitingList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long trainId;

    private Long bookingId;

    private Integer priorityNumber;

    private String status; // WL / MOVED_TO_RAC / CONFIRMED
}