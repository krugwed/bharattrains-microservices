package com.train.trainservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "station")
@Data
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "station_seq_gen")
    @SequenceGenerator(name = "station_seq_gen", sequenceName = "station_seq", allocationSize = 1)
    private Long stationId;

    private String name;

    private String code; // PUNE, MUM, DEL
}