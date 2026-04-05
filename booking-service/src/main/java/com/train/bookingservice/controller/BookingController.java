package com.train.bookingservice.controller;

import com.train.bookingservice.dto.BookingDetailsResponse;
import com.train.bookingservice.dto.BookingRequestDTO;
import com.train.bookingservice.entity.Booking;
import com.train.bookingservice.repository.BookingRepository;
import com.train.bookingservice.service.BookingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;

    private final BookingRepository  bookingRepository;

    public BookingController(BookingService bookingService,  BookingRepository bookingRepository) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
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

    @PutMapping("/{bookingId}/status")
    public void updateStatus(@PathVariable Long bookingId,
                             @RequestParam String status) {

        Booking booking = bookingRepository.findById(bookingId).orElseThrow();

        booking.setPaymentStatus(status);
        bookingRepository.save(booking);
    }
}