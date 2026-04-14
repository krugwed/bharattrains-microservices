package com.train.inventory.entity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "WAITING_LIST")
@Data
public class WaitingList {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "waiting_list_seq_gen")
    @SequenceGenerator(name = "waiting_list_seq_gen", sequenceName = "waiting_list_seq", allocationSize = 1)
    private Long id;

    private Long trainId;

    private Long bookingId;

    private Integer priorityNumber;

    private Long passengerId;

    private String status; // WL / MOVED_TO_RAC / CONFIRMED
}