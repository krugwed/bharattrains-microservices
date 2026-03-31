package com.train.inventory.repository;

import com.train.inventory.entity.SeatAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SeatAllocationRepository extends JpaRepository<SeatAllocation, Long> {

    List<SeatAllocation> findBySeatIdAndTrainIdAndJourneyDate(
            Long seatId,
            Long trainId,
            LocalDate journeyDate
    );

    long countByTrainIdAndJourneyDateAndStatus(Long trainId, LocalDate date, String status);

    List<SeatAllocation> findByTrainIdAndJourneyDateAndStatusOrderByAllocationIdAsc(
            Long trainId, LocalDate date, String status
    );

    List<SeatAllocation> findByBookingId(Long bookingId);

    List<SeatAllocation> findByPassengerId(Long passengerId);

}