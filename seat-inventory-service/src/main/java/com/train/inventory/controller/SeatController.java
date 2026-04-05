package com.train.inventory.controller;

import com.train.inventory.entity.AvailabilityRequest;
import com.train.inventory.service.SeatAllocationService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/seats")
public class SeatController {

    private final SeatAllocationService seatAllocationService;

    public SeatController(SeatAllocationService seatAllocationService) {
        this.seatAllocationService = seatAllocationService;
    }

    @PostMapping("/book")
    public String bookSeat(@RequestParam Long trainId,
                                       @RequestParam String journeyDate,
                                       @RequestParam int fromStation,
                                       @RequestParam int toStation,
                                       @RequestParam Long bookingId,
                                        @RequestParam Long passengerId) {

        return seatAllocationService.bookSeat(trainId,
                LocalDate.parse(journeyDate),
                fromStation,
                toStation,
                bookingId,
                passengerId
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

    @PostMapping("/availability/bulk")
    public Map<Long, Integer> getBulkAvailability(
            @RequestBody List<AvailabilityRequest> requests) {

        return seatAllocationService.getBulkAvailability(requests);
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

    @PostMapping("/cancel/passenger")
    public String cancelPassenger(
            @RequestParam Long passengerId,
            @RequestParam Long trainId,
            @RequestParam String date) {

        seatAllocationService.cancelPassenger(
                passengerId,
                trainId,
                LocalDate.parse(date)
        );

        return "Passenger seat cancelled";
    }

}