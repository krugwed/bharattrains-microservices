package com.train.bookingservice.controller;

import com.train.bookingservice.dto.BookingDetailsResponse;
import com.train.bookingservice.dto.BookingRequestDTO;
import com.train.bookingservice.entity.Booking;
import com.train.bookingservice.service.BookingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/create")
    public Booking createBooking(@RequestBody BookingRequestDTO request) {
        return bookingService.createBooking(request);
    }

    @GetMapping("/pnr/{pnr}")
    public BookingDetailsResponse getPNR(@PathVariable String pnr) {

        return bookingService.getBookingByPNR(pnr);
    }

    @DeleteMapping("/cancel/{pnr}")
    public String cancelBooking(@PathVariable String pnr) {
        return bookingService.cancelBooking(pnr);
    }

    @DeleteMapping("/passenger/{passengerId}")
    public String cancelPassenger(@PathVariable Long passengerId) {
        return bookingService.cancelPassenger(passengerId);
    }

}