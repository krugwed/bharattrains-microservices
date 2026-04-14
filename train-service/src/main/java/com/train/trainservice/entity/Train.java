package com.train.trainservice.entity;

import com.train.trainservice.converter.DayOfWeekSetConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.time.DayOfWeek;
import java.util.Set;

@Entity
@Table(name = "train")
@Data
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "train_seq_gen")
    @SequenceGenerator(name = "train_seq_gen", sequenceName = "train_seq", allocationSize = 1)
    private Long trainId;

    private String trainName;

    @Convert(converter = DayOfWeekSetConverter.class)
    private Set<DayOfWeek> runningDays;

}