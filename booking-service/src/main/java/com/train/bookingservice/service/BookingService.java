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

            String status = seatClient.bookSeat(
                    request.getTrainId(),
                    request.getJourneyDate(),
                    request.getFromStation(),
                    request.getToStation(),
                    booking.getBookingId()
            );

            Passenger passenger = new Passenger();
            passenger.setBookingId(booking.getBookingId());
            passenger.setName(p.getName());
            passenger.setAge(p.getAge());
            passenger.setGender(p.getGender());
            passenger.setStatus(status);

            if (!status.equals("CONFIRMED")) {
                allConfirmed = false;
            }

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
}