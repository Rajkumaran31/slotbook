package com.slotbook.repo;

import com.slotbook.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepo extends JpaRepository<Venue, Long> {}
