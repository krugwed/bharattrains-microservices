package com.train.bookingservice.service;

import com.train.bookingservice.client.NotificationClient;
import com.train.bookingservice.client.SeatInventoryClient;
import com.train.bookingservice.client.TrainClient;
import com.train.bookingservice.client.UserClient;
import com.train.bookingservice.dto.*;
import com.train.bookingservice.entity.Booking;
import com.train.bookingservice.entity.Passenger;
import com.train.bookingservice.repository.BookingRepository;
import com.train.bookingservice.repository.PassengerRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final SeatInventoryClient seatClient;
    private final NotificationClient notificationClient;
    private final UserClient userClient;
    private final TrainClient trainClient;
    private final KafkaProducerService kafkaProducerService;

    public BookingService(
            BookingRepository bookingRepository,
            PassengerRepository passengerRepository,
            SeatInventoryClient seatClient,
            NotificationClient notificationClient,
            UserClient userClient,
            TrainClient trainClient, KafkaProducerService kafkaProducerService) {
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.seatClient = seatClient;
        this.notificationClient = notificationClient;
        this.userClient = userClient;
        this.trainClient = trainClient;
        this.kafkaProducerService = kafkaProducerService;
    }

    public Booking createBooking(BookingRequestDTO request) {

        // Fetch email from User Service
        String email = userClient.getUserEmail(request.getUserId());

        Double farePerPassenger = trainClient.getFare(
                request.getTrainId(),
                String.valueOf(request.getFromStation()),
                String.valueOf(request.getToStation()),
                request.getCoachType(),
                request.getJourneyDate().toString()
        );
        // Create booking
        Booking booking = new Booking();
        booking.setTrainId(request.getTrainId());
        booking.setJourneyDate(request.getJourneyDate());
        booking.setFromStation(request.getFromStation());
        booking.setToStation(request.getToStation());
        booking.setPnr(generatePNR());
        booking.setStatus("CREATED");
        booking.setPaymentStatus("PENDING");

        bookingRepository.save(booking);

        boolean allConfirmed = true;
        double totalFare = 0;

        for (PassengerDTO p : request.getPassengers()) {

            Passenger passenger = new Passenger();
            passenger.setBookingId(booking.getBookingId());
            passenger.setName(p.getName());
            passenger.setAge(p.getAge());
            passenger.setGender(p.getGender());
            passenger.setFare(farePerPassenger);

            passengerRepository.save(passenger);

            String status = seatClient.bookSeat(
                    request.getTrainId(),
                    request.getJourneyDate(),
                    request.getFromStation(),
                    request.getToStation(),
                    booking.getBookingId(),
                    passenger.getPassengerId()
            );

            passenger.setStatus(status);

            if (!"CONFIRMED".equals(status)) {
                allConfirmed = false;
            }
            passengerRepository.save(passenger);

            totalFare += farePerPassenger;

        }
        booking.setTotalFare(totalFare);

        // Update booking status
        booking.setStatus(allConfirmed ? "CONFIRMED" : "PARTIAL");
        bookingRepository.save(booking);

        // SEND NOTIFICATION
        BookingEvent event = new BookingEvent();
        event.setBookingId(booking.getBookingId());
        event.setMessage("Booking created successfully. PNR: " + booking.getPnr()
                + " | Total Fare: " + totalFare);
        event.setEmail(email);
        event.setType("BOOKING_CREATED");

        kafkaProducerService.sendMessage(
                "booking-topic",
                new ObjectMapper().writeValueAsString(event)
        );

//        notificationClient.sendNotification(
//                booking.getBookingId(),
//                "Booking created successfully. PNR: " + booking.getPnr()
//                        + " | Total Fare: " + totalFare,
//                email,
//                "BOOKING_CREATED"
//        );

        return booking;
    }

    private String generatePNR() {
        return "PNR" + System.currentTimeMillis();
    }

    public BookingDetailsResponse getBookingByPNR(String pnr) {

        Booking booking = bookingRepository.findByPnr(pnr);

        if (booking == null) {
            throw new RuntimeException("PNR not found");
        }

        List<Passenger> passengers =
                passengerRepository.findByBookingId(booking.getBookingId());
        List<SeatDetails> seatDetails =
                seatClient.getSeatDetails(booking.getBookingId());

        List<PassengerDetails> passengerDetailsList =
                passengers.stream().map(p -> {

                    SeatDetails seat = seatDetails.stream()
                            .filter(s -> s.getPassengerId().equals(p.getPassengerId()))
                            .findFirst()
                            .orElse(null);

                    String seatValue = null;

                    if (seat != null && seat.getCoachNumber() != null) {
                        seatValue = seat.getCoachNumber() + "-" + seat.getSeatNumber();
                    }

                    return new PassengerDetails(
                            p.getName(),
                            p.getAge(),
                            p.getGender(),
                            p.getStatus(),
                            seatValue
                    );

                }).toList();

        return new BookingDetailsResponse(
                booking.getPnr(),
                booking.getTrainId(),
                booking.getJourneyDate(),
                booking.getFromStation(),
                booking.getToStation(),
                booking.getStatus(),
                booking.getTotalFare(),
                passengerDetailsList
        );
    }

    @Transactional
    public String cancelBooking(String pnr) {

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

        List<Passenger> passengers =
                passengerRepository.findByBookingId(booking.getBookingId());

        for (Passenger p : passengers) {
            p.setStatus("CANCELLED");
            passengerRepository.save(p);
        }

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        // Fetch email from User Service
        String email = userClient.getUserEmailByBookingId(booking.getBookingId());

        //  SEND NOTIFICATION
        BookingEvent event = new BookingEvent();
        event.setBookingId(booking.getBookingId());
        event.setMessage("Your booking with PNR " + booking.getPnr() + " has been cancelled.");
        event.setEmail(email);
        event.setType("BOOKING_CANCELLED");

        kafkaProducerService.sendMessage(
                "booking-topic",
                new ObjectMapper().writeValueAsString(event)
        );

//        notificationClient.sendNotification(
//                booking.getBookingId(),
//                "Your booking with PNR " + booking.getPnr() + " has been cancelled.",
//                email,
//                "BOOKING_CANCELLED"
//        );

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

        Booking booking =
                bookingRepository.findById(passenger.getBookingId())
                        .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Call seat service
        seatClient.cancelPassenger(
                passengerId,
                booking.getTrainId(),
                booking.getJourneyDate()
        );

        passenger.setStatus("CANCELLED");
        passengerRepository.save(passenger);

        updateBookingStatus(booking.getBookingId());

        // Fetch email
        String email = userClient.getUserEmailByBookingId(booking.getBookingId());

        // SEND NOTIFICATION
        BookingEvent event = new BookingEvent();
        event.setBookingId(booking.getBookingId());
        event.setMessage("Your booking with PNR " + booking.getPnr() + " has been cancelled.");
        event.setEmail(email);
        event.setType("PASSENGER_CANCELLED");

        kafkaProducerService.sendMessage(
                "booking-topic",
                new ObjectMapper().writeValueAsString(event)
        );

//        notificationClient.sendNotification(
//                booking.getBookingId(),
//                "Passenger " + passenger.getName() + " has been cancelled from booking PNR: " + booking.getPnr(),
//                email,
//                "PASSENGER_CANCELLED"
//        );

        return "Passenger cancelled successfully";
    }

    private void updateBookingStatus(Long bookingId) {

        List<Passenger> passengers =
                passengerRepository.findByBookingId(bookingId);

        boolean allCancelled =
                passengers.stream().allMatch(p -> p.getStatus().equals("CANCELLED"));

        boolean allConfirmed =
                passengers.stream().allMatch(p -> p.getStatus().equals("CONFIRMED"));

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