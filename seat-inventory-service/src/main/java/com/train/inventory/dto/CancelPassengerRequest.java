package com.train.inventory.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CancelPassengerRequest {
    private Long passengerId;
    private Long trainId;
    private LocalDate date;

    // getters and setters
}

