package com.train.inventory.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "coach")
@Data
public class Coach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long coachId;

    private Long trainId;

    private String coachNumber; // S1, S2, B1

    private String coachType; // Sleeper, AC3, AC2

    private int totalSeats;
}