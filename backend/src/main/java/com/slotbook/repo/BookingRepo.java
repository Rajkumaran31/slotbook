package com.slotbook.repo;

import com.slotbook.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface BookingRepo extends JpaRepository<Booking, Long> {
    List<Booking> findByVenueIdAndBookingDate(Long venueId, LocalDate date);
    List<Booking> findByUserIdOrderByBookingDateDescSlotHourAsc(Long userId);
}
