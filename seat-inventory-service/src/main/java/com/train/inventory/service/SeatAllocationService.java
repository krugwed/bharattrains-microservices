package com.train.inventory.service;

import com.train.inventory.dto.AvailabilityRequest;
import com.train.inventory.dto.SeatDetails;
import com.train.inventory.dto.SeatSetupRequest;
import com.train.inventory.entity.*;
import com.train.inventory.repository.CoachRepository;
import com.train.inventory.repository.SeatAllocationRepository;
import com.train.inventory.repository.SeatRepository;
import com.train.inventory.repository.WaitingListRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeatAllocationService {

    private final SeatRepository seatRepository;
    private final SeatAllocationRepository seatAllocationRepository;
    private final WaitingListRepository waitingListRepository;
    private final CoachRepository coachRepository;

    public SeatAllocationService(
            SeatRepository seatRepository,
            SeatAllocationRepository seatAllocationRepository,
            WaitingListRepository waitingListRepository,
            CoachRepository coachRepository
    ) {
        this.seatRepository = seatRepository;
        this.seatAllocationRepository = seatAllocationRepository;
        this.waitingListRepository = waitingListRepository;
        this.coachRepository = coachRepository;
    }

    private boolean isSeatAvailable(List<SeatAllocation> allocations,
                                    int newFrom,
                                    int newTo) {

        for (SeatAllocation allocation : allocations) {

            int existingFrom = allocation.getFromStationOrder();
            int existingTo = allocation.getToStationOrder();

            boolean overlap =
                    newFrom < existingTo && newTo > existingFrom;

            if (overlap) {
                return false;
            }
        }

        return true;
    }

    public int checkSeatAvailability(Long trainId,
                                     LocalDate journeyDate,
                                     int fromStationOrder,
                                     int toStationOrder) {

        List<Coach> coaches = coachRepository.findByTrainId(trainId);

        List<Seat> seats = coaches.stream()
                .flatMap(coach -> seatRepository.findByCoachId(coach.getCoachId()).stream())
                .toList();
        int availableSeats = 0;

        for (Seat seat : seats) {

            List<SeatAllocation> allocations =
                    seatAllocationRepository.findBySeatIdAndTrainIdAndJourneyDate(
                            seat.getSeatId(),
                            trainId,
                            journeyDate
                    );

            boolean available = isSeatAvailable(
                    allocations,
                    fromStationOrder,
                    toStationOrder
            );

            if (available) {
                availableSeats++;
            }
        }

        return availableSeats;
    }

    private long countConfirmed(Long trainId, LocalDate date) {
        return seatAllocationRepository.countByTrainIdAndJourneyDateAndStatus(
                trainId, date, "CONFIRMED"
        );
    }

    private long countRAC(Long trainId, LocalDate date) {
        return seatAllocationRepository.countByTrainIdAndJourneyDateAndStatus(
                trainId, date, "RAC"
        );
    }

    public String bookSeat(Long trainId,
                           LocalDate date,
                           int from,
                           int to,
                           Long bookingId,
                           Long passengerId) {

        // 1. Try confirmed seat
        SeatAllocation allocation = tryAllocateConfirmed(trainId, date, from, to, bookingId, passengerId);

        if (allocation != null) {
            return "CONFIRMED";
        }

        // 2. Try RAC
        boolean racAllocated = allocateRAC(trainId, date, bookingId, passengerId);

        if (racAllocated) {
            return "RAC";
        }

        // 3. Add to WL
        addToWaitingList(trainId, bookingId, passengerId);

        return "WAITING_LIST";
    }

    private boolean allocateRAC(Long trainId, LocalDate date, Long bookingId, Long passengerId) {

        long racCount = countRAC(trainId, date);

        int RAC_LIMIT = 20; // configurable

        if (racCount < RAC_LIMIT) {

            SeatAllocation sa = new SeatAllocation();
            sa.setTrainId(trainId);
            sa.setJourneyDate(date);
            sa.setBookingId(bookingId);
            sa.setPassengerId(passengerId);
            sa.setStatus("RAC");

            seatAllocationRepository.save(sa);

            return true;
        }

        return false;
    }

    private void addToWaitingList(Long trainId, Long bookingId, Long passengerId) {

        long count = waitingListRepository.countByTrainId(trainId);

        WaitingList wl = new WaitingList();
        wl.setTrainId(trainId);
        wl.setBookingId(bookingId);
        wl.setPriorityNumber((int) count + 1);
        wl.setPassengerId(passengerId);
        wl.setStatus("WL");

        waitingListRepository.save(wl);
    }

    private SeatAllocation tryAllocateConfirmed(Long trainId,
                                                LocalDate journeyDate,
                                                int fromStationOrder,
                                                int toStationOrder,
                                                Long bookingId,
                                                Long passengerId) {

        List<Coach> coaches = coachRepository.findByTrainId(trainId);

        List<Seat> seats = coaches.stream()
                .flatMap(coach -> seatRepository.findByCoachId(coach.getCoachId()).stream())
                .toList();

        for (Seat seat : seats) {

            List<SeatAllocation> allocations =
                    seatAllocationRepository.findBySeatIdAndTrainIdAndJourneyDate(
                            seat.getSeatId(), trainId, journeyDate);

            boolean available = isSeatAvailable(
                    allocations,
                    fromStationOrder,
                    toStationOrder
            );

            if (available) {

                SeatAllocation allocation = new SeatAllocation();
                allocation.setSeatId(seat.getSeatId());
                allocation.setTrainId(trainId);
                allocation.setJourneyDate(journeyDate);
                allocation.setFromStationOrder(fromStationOrder);
                allocation.setToStationOrder(toStationOrder);
                allocation.setBookingId(bookingId);
                allocation.setPassengerId(passengerId);
                allocation.setStatus("CONFIRMED");

                return seatAllocationRepository.save(allocation);
            }
        }

        return null;
    }

    private void promotePassengers(Long trainId, LocalDate date) {

        List<SeatAllocation> racList =
                seatAllocationRepository
                        .findByTrainIdAndJourneyDateAndStatusOrderByAllocationIdAsc(
                                trainId, date, "RAC"
                        );

        if (!racList.isEmpty()) {

            SeatAllocation racPassenger = racList.get(0);

            List<Coach> coaches = coachRepository.findByTrainId(trainId);

            List<Seat> seats = coaches.stream()
                    .flatMap(coach -> seatRepository.findByCoachId(coach.getCoachId()).stream())
                    .toList();

            for (Seat seat : seats) {

                List<SeatAllocation> allocations =
                        seatAllocationRepository
                                .findBySeatIdAndTrainIdAndJourneyDate(
                                        seat.getSeatId(), trainId, date
                                );

                boolean available = isSeatAvailable(
                        allocations,
                        racPassenger.getFromStationOrder(),
                        racPassenger.getToStationOrder()
                );

                if (available) {

                    racPassenger.setSeatId(seat.getSeatId());
                    racPassenger.setStatus("CONFIRMED");

                    seatAllocationRepository.save(racPassenger);
                    break;
                }
            }
        }

        // WL → RAC
        List<WaitingList> wlList =
                waitingListRepository.findByTrainIdOrderByPriorityNumberAsc(trainId);

        if (!wlList.isEmpty()) {

            WaitingList wl = wlList.get(0);

            wl.setStatus("MOVED_TO_RAC");
            waitingListRepository.save(wl);

            allocateRAC(trainId, date, wl.getBookingId(), wl.getPassengerId());
        }
    }

    public void cancelBooking(Long bookingId, Long trainId, LocalDate date) {

        // 1. Find allocation
        List<SeatAllocation> allocations =
                seatAllocationRepository.findByBookingId(bookingId);

        if (allocations.isEmpty()) {
            throw new RuntimeException("Booking not found");
        }

        // 2. Cancel allocation
        for (SeatAllocation allocation : allocations) {
            allocation.setStatus("CANCELLED");
            seatAllocationRepository.save(allocation);
        }

        // 3. Trigger promotion
        promotePassengers(trainId, date);
    }

    public void cancelPassenger(Long passengerId,
                                Long trainId,
                                LocalDate date) {

        List<SeatAllocation> allocations =
                seatAllocationRepository.findByPassengerId(passengerId);

        if (allocations.isEmpty()) {
            throw new RuntimeException("Passenger allocation not found");
        }

        for (SeatAllocation allocation : allocations) {
            allocation.setStatus("CANCELLED");
            seatAllocationRepository.save(allocation);
        }

        // Trigger promotion
        promotePassengers(trainId, date);
    }

    public Map<Long, Integer> getBulkAvailability(
            List<AvailabilityRequest> requests) {

        Map<Long, Integer> result = new HashMap<>();

        for (AvailabilityRequest req : requests) {

            int count = checkSeatAvailability(
                    req.getTrainId(),
                    req.getJourneyDate(),
                    req.getFrom(),
                    req.getTo()
            );

            result.put(req.getTrainId(), count);
        }

        return result;
    }

    public List<SeatDetails> getSeatDetailsByBooking(Long bookingId) {

        List<SeatAllocation> allocations =
                seatAllocationRepository.findByBookingId(bookingId);

        return allocations.stream().map(a -> {

            Seat seat = seatRepository.findById(a.getSeatId()).orElse(null);
            Coach coach = coachRepository.findById(seat.getCoachId()).orElse(null);

            SeatDetails dto = new SeatDetails();
            dto.setPassengerId(a.getPassengerId());
            dto.setSeatNumber(seat.getSeatNumber());
            dto.setCoachNumber(coach.getCoachNumber());

            return dto;

        }).toList();
    }

    public void createSeatsForTrain(SeatSetupRequest request) {

        Long trainId = request.getTrainId();

        // Sleeper
        createCoaches(trainId, "S", request.getSleeperCoaches(), request.getSeatsPerCoach());

        // AC3
        createCoaches(trainId, "B", request.getAc3Coaches(), request.getSeatsPerCoach());

        // AC2
        createCoaches(trainId, "A", request.getAc2Coaches(), request.getSeatsPerCoach());
    }

    private void createCoaches(Long trainId, String prefix,
                               int coachCount, int seatsPerCoach) {

        for (int i = 1; i <= coachCount; i++) {

            Coach coach = new Coach();
            coach.setTrainId(trainId);
            coach.setCoachNumber(prefix + i);

            coachRepository.save(coach);

            for (int j = 1; j <= seatsPerCoach; j++) {

                Seat seat = new Seat();
                seat.setCoachId(coach.getCoachId());
                seat.setSeatNumber(String.valueOf(j));

                seatRepository.save(seat);
            }
        }
    }
}