package com.train.bookingservice.repository;

import com.train.bookingservice.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Booking findByPnr(String pnr);
}