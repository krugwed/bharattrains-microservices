package com.train.notificationservice.dto;

import lombok.Data;

@Data
public class BookingEvent {

    private Long bookingId;
    private String message;
    private String email;
    private String type;

}
