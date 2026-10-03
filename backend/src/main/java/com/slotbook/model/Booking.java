package com.slotbook.model;

import jakarta.persistence.*;
import java.time.LocalDate;

// UNIQUE constraint = database-level guarantee against double booking
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"venueId", "bookingDate", "slotHour"}))
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public Long userId, venueId;
    public LocalDate bookingDate;
    public int slotHour;      // 6..21  (06:00 - 22:00)
    public int price;
    public String code;       // ticket code shown to the user
    public String venueName, emoji;
}
