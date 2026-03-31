package com.train.bookingservice.service;

import com.train.bookingservice.client.SeatInventoryClient;
import com.train.bookingservice.dto.BookingDetailsResponse;
import com.train.bookingservice.dto.BookingRequestDTO;
import com.train.bookingservice.dto.PassengerDTO;
import com.train.bookingservice.dto.PassengerDetails;
import com.train.bookingservice.entity.Booking;
import com.train.bookingservice.entity.Passenger;
import com.train.bookingservice.repository.BookingRepository;
import com.train.bookingservice.repository.PassengerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final SeatInventoryClient seatClient;

    public BookingService(BookingRepository bookingRepository,
                          PassengerRepository passengerRepository,
                          SeatInventoryClient seatClient) {
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.seatClient = seatClient;
    }

    public Booking createBooking(BookingRequestDTO request) {

        //  Create booking
        Booking booking = new Booking();
        booking.setTrainId(request.getTrainId());
        booking.setJourneyDate(request.getJourneyDate());
        booking.setFromStation(request.getFromStation());
        booking.setToStation(request.getToStation());
        booking.setPnr(generatePNR());
        booking.setStatus("CREATED");

        bookingRepository.save(booking);

        //Allocate seats per passenger
        boolean allConfirmed = true;

        for (PassengerDTO p : request.getPassengers()) {

            Passenger passenger = new Passenger();
            passenger.setBookingId(booking.getBookingId());
            passenger.setName(p.getName());
            passenger.setAge(p.getAge());
            passenger.setGender(p.getGender());

            passengerRepository.save(passenger); // ✅ FIRST SAVE

            String status = seatClient.bookSeat(
                    request.getTrainId(),
                    request.getJourneyDate(),
                    request.getFromStation(),
                    request.getToStation(),
                    booking.getBookingId(),
                    passenger.getPassengerId()   // ✅ pass this
            );

            passenger.setStatus(status);
            passengerRepository.save(passenger);
        }

        //Update booking status
        booking.setStatus(allConfirmed ? "CONFIRMED" : "PARTIAL");
        bookingRepository.save(booking);

        return booking;
    }

    private String generatePNR() {
        return "PNR" + System.currentTimeMillis();
    }

    public BookingDetailsResponse getBookingByPNR(String pnr) {

        //Fetch booking
        Booking booking = bookingRepository.findByPnr(pnr);

        if (booking == null) {
            throw new RuntimeException("PNR not found");
        }

        // Fetch passengers
        List<Passenger> passengers =
                passengerRepository.findByBookingId(booking.getBookingId());

        // Map passengers → DTO
        List<PassengerDetails> passengerDetailsList =
                passengers.stream()
                        .map(p -> new PassengerDetails(
                                p.getName(),
                                p.getAge(),
                                p.getGender(),
                                p.getStatus()
                        ))
                        .toList();

        //  Build response
        return new BookingDetailsResponse(
                booking.getPnr(),
                booking.getTrainId(),
                booking.getJourneyDate(),
                booking.getFromStation(),
                booking.getToStation(),
                booking.getStatus(),
                passengerDetailsList
        );
    }

    @Transactional
    public String cancelBooking(String pnr) {

        // Find booking
        Booking booking = bookingRepository.findByPnr(pnr);

        if (booking == null) {
            throw new RuntimeException("PNR not found");
        }

        if ("CANCELLED".equals(booking.getStatus())) {
            return "Booking already cancelled";
        }

        // Call seat service
        seatClient.cancelBooking(
                booking.getBookingId(),
                booking.getTrainId(),
                booking.getJourneyDate()
        );

        //Update passengers
        List<Passenger> passengers =
                passengerRepository.findByBookingId(booking.getBookingId());

        for (Passenger p : passengers) {
            p.setStatus("CANCELLED");
            passengerRepository.save(p);
        }

        //Update booking
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        return "Booking cancelled successfully";
    }

    @Transactional
    public String cancelPassenger(Long passengerId) {

        Passenger passenger =
                passengerRepository.findById(passengerId)
                        .orElseThrow(() -> new RuntimeException("Passenger not found"));

        if ("CANCELLED".equals(passenger.getStatus())) {
            return "Passenger already cancelled";
        }

        //Call seat service
        Booking booking = bookingRepository.findById(passenger.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        seatClient.cancelPassenger(
                passengerId,
                booking.getTrainId(),
                booking.getJourneyDate()
        );

        // Update passenger
        passenger.setStatus("CANCELLED");
        passengerRepository.save(passenger);

        // Recalculate booking status
        updateBookingStatus(booking.getBookingId());

        return "Passenger cancelled successfully";
    }

    private void updateBookingStatus(Long bookingId) {

        List<Passenger> passengers =
                passengerRepository.findByBookingId(bookingId);

        boolean allCancelled = passengers.stream()
                .allMatch(p -> p.getStatus().equals("CANCELLED"));

        boolean allConfirmed = passengers.stream()
                .allMatch(p -> p.getStatus().equals("CONFIRMED"));

        Booking booking = bookingRepository.findById(bookingId).get();

        if (allCancelled) {
            booking.setStatus("CANCELLED");
        } else if (allConfirmed) {
            booking.setStatus("CONFIRMED");
        } else {
            booking.setStatus("PARTIAL");
        }

        bookingRepository.save(booking);
    }

}