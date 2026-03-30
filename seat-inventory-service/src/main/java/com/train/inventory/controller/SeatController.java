package com.train.inventory.controller;

import com.train.inventory.entity.SeatAllocation;
import com.train.inventory.service.SeatAllocationService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/seats")
public class SeatController {

    private final SeatAllocationService seatAllocationService;

    public SeatController(SeatAllocationService seatAllocationService) {
        this.seatAllocationService = seatAllocationService;
    }

    @PostMapping("/allocate")
    public String allocateSeat(@RequestParam Long trainId,
                                       @RequestParam String journeyDate,
                                       @RequestParam int fromStation,
                                       @RequestParam int toStation,
                                       @RequestParam Long bookingId) {

        return seatAllocationService.bookSeat(trainId,
                LocalDate.parse(journeyDate),
                fromStation,
                toStation,
                bookingId
        );
    }

    @GetMapping("/availability")
    public int checkAvailability(
            @RequestParam Long trainId,
            @RequestParam String journeyDate,
            @RequestParam int fromStation,
            @RequestParam int toStation) {

        return seatAllocationService.checkSeatAvailability(
                trainId,
                LocalDate.parse(journeyDate),
                fromStation,
                toStation
        );
    }

    @PostMapping("/cancel")
    public String cancelBooking(
            @RequestParam Long bookingId,
            @RequestParam Long trainId,
            @RequestParam String date) {

        seatAllocationService.cancelBooking(
                bookingId,
                trainId,
                LocalDate.parse(date)
        );

        return "Booking cancelled and promotion processed";
    }
}