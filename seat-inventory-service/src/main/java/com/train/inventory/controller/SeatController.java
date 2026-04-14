package com.train.inventory.controller;

import com.train.inventory.dto.*;
import com.train.inventory.dto.AvailabilityRequest;
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
    public String bookSeat(@RequestBody SeatBookingRequest request) {

        return seatAllocationService.bookSeat(
                request.getTrainId(),
                request.getJourneyDate(),
                request.getFromStation(),
                request.getToStation(),
                request.getBookingId(),
                request.getPassengerId()
        );
    }

    @PostMapping("/availability")
    public int checkAvailability(@RequestBody AvailabilityCheckRequest request) {

        return seatAllocationService.checkSeatAvailability(
                request.getTrainId(),
                LocalDate.parse(request.getJourneyDate()),
                request.getFromStation(),
                request.getToStation()
        );
    }

    @PostMapping("/availability/bulk")
    public Map<Long, Integer> getBulkAvailability(
            @RequestBody List<AvailabilityRequest> requests) {

        return seatAllocationService.getBulkAvailability(requests);
    }

    @PostMapping("/cancel")
    public String cancelBooking(@RequestBody CancelBookingRequest request) {

        seatAllocationService.cancelBooking(
                request.getBookingId(),
                request.getTrainId(),
                request.getDate()
        );

        return "Booking cancelled and promotion processed";
    }

    @PostMapping("/cancel/passenger")
    public String cancelPassenger(@RequestBody CancelPassengerRequest request) {

        seatAllocationService.cancelPassenger(
                request.getPassengerId(),
                request.getTrainId(),
                request.getDate()
        );

        return "Passenger seat cancelled";
    }

    @PostMapping("/setup")
    public String setupSeats(@RequestBody SeatSetupRequest request) {
        seatAllocationService.createSeatsForTrain(request);
        return "Seats created";
    }

}