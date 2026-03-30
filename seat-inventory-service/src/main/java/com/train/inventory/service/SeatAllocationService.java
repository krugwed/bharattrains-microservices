package com.train.inventory.service;

import com.train.inventory.entity.Seat;
import com.train.inventory.entity.SeatAllocation;
import com.train.inventory.entity.WaitingList;
import com.train.inventory.repository.SeatAllocationRepository;
import com.train.inventory.repository.SeatRepository;
import com.train.inventory.repository.WaitingListRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SeatAllocationService {

    private final SeatRepository seatRepository;
    private final SeatAllocationRepository seatAllocationRepository;
    private final WaitingListRepository waitingListRepository;

    public SeatAllocationService(
            SeatRepository seatRepository,
            SeatAllocationRepository seatAllocationRepository,
            WaitingListRepository waitingListRepository) {

        this.seatRepository = seatRepository;
        this.seatAllocationRepository = seatAllocationRepository;
        this.waitingListRepository = waitingListRepository;
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

        List<Seat> seats = seatRepository.findAll();
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
                           Long bookingId) {

        // 1. Try confirmed seat
        SeatAllocation allocation = tryAllocateConfirmed(trainId, date, from, to, bookingId);

        if (allocation != null) {
            return "CONFIRMED";
        }

        // 2. Try RAC
        boolean racAllocated = allocateRAC(trainId, date, bookingId);

        if (racAllocated) {
            return "RAC";
        }

        // 3. Add to WL
        addToWaitingList(trainId, bookingId);

        return "WAITING_LIST";
    }

    private boolean allocateRAC(Long trainId, LocalDate date, Long bookingId) {

        long racCount = countRAC(trainId, date);

        int RAC_LIMIT = 20; // configurable

        if (racCount < RAC_LIMIT) {

            SeatAllocation sa = new SeatAllocation();
            sa.setTrainId(trainId);
            sa.setJourneyDate(date);
            sa.setBookingId(bookingId);
            sa.setStatus("RAC");

            seatAllocationRepository.save(sa);

            return true;
        }

        return false;
    }

    private void addToWaitingList(Long trainId, Long bookingId) {

        long count = waitingListRepository.countByTrainId(trainId);

        WaitingList wl = new WaitingList();
        wl.setTrainId(trainId);
        wl.setBookingId(bookingId);
        wl.setPriorityNumber((int) count + 1);
        wl.setStatus("WL");

        waitingListRepository.save(wl);
    }

    private SeatAllocation tryAllocateConfirmed(Long trainId,
                                                LocalDate journeyDate,
                                                int fromStationOrder,
                                                int toStationOrder,
                                                Long bookingId) {

        List<Seat> seats = seatRepository.findAll();

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

            List<Seat> seats = seatRepository.findAll();

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

            allocateRAC(trainId, date, wl.getBookingId());
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

}