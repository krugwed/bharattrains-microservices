package com.train.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassengerDetails {

    private String name;
    private int age;
    private String gender;
    private String status;
    private String seat;
}