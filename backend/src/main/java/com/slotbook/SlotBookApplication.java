package com.slotbook;

import com.slotbook.model.Venue;
import com.slotbook.repo.VenueRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SlotBookApplication {
    public static void main(String[] args) { SpringApplication.run(SlotBookApplication.class, args); }

    // Seeds sample venues on first run
    @Bean CommandLineRunner seed(VenueRepo repo) {
        return args -> {
            if (repo.count() > 0) return;
            repo.save(new Venue("Floodlight Arena", "Coimbatore", "Football", 1200, "⚽", "5-a-side turf with LED floodlights"));
            repo.save(new Venue("Smash Court 9", "Coimbatore", "Badminton", 400, "🏸", "Wooden indoor court, 4 courts"));
            repo.save(new Venue("Boundary Nets", "Coimbatore", "Cricket", 900, "🏏", "Box cricket with bowling machine"));
            repo.save(new Venue("Topspin Club", "Coimbatore", "Table Tennis", 250, "🏓", "Air-conditioned, pro tables"));
            repo.save(new Venue("Hoop Yard", "Coimbatore", "Basketball", 700, "🏀", "Half court, evening lights"));
            repo.save(new Venue("Serve & Volley", "Coimbatore", "Tennis", 800, "🎾", "Clay court with coaching option"));
        };
    }
}
