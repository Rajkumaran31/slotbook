package com.slotbook.web;

import com.slotbook.model.*;
import com.slotbook.repo.*;
import com.slotbook.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController @RequestMapping("/api")
public class ApiController {
    private final UserRepo users; private final VenueRepo venues; private final BookingRepo bookings; private final JwtUtil jwt;
    private final BCryptPasswordEncoder enc = new BCryptPasswordEncoder();

    public ApiController(UserRepo u, VenueRepo v, BookingRepo b, JwtUtil j) { users = u; venues = v; bookings = b; jwt = j; }

    record AuthReq(String name, String email, String password) {}
    record BookReq(Long venueId, LocalDate date, int hour) {}

    private ResponseEntity<?> err(HttpStatus s, String msg) { return ResponseEntity.status(s).body(Map.of("message", msg)); }
    private Map<String, Object> authBody(User u) { return Map.of("token", jwt.create(u.id), "name", u.name); }

    // ---------- AUTH ----------
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody AuthReq r) {
        if (r.email() == null || r.password() == null || r.password().length() < 6) return err(HttpStatus.BAD_REQUEST, "Use a valid email and a password of 6+ characters");
        if (users.findByEmail(r.email()).isPresent()) return err(HttpStatus.CONFLICT, "Email already registered");
        return ResponseEntity.ok(authBody(users.save(new User(r.name(), r.email(), enc.encode(r.password())))));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody AuthReq r) {
        Optional<User> u = users.findByEmail(r.email());
        if (u.isEmpty() || !enc.matches(r.password(), u.get().password)) return err(HttpStatus.UNAUTHORIZED, "Wrong email or password");
        return ResponseEntity.ok(authBody(u.get()));
    }

    // ---------- VENUES ----------
    @GetMapping("/venues") public List<Venue> venues() { return venues.findAll(); }

    // Returns the hours already taken on a date
    @GetMapping("/venues/{id}/slots")
    public List<Integer> taken(@PathVariable Long id, @RequestParam LocalDate date) {
        return bookings.findByVenueIdAndBookingDate(id, date).stream().map(b -> b.slotHour).collect(Collectors.toList());
    }

    // ---------- BOOKINGS ----------
    @PostMapping("/bookings")
    public ResponseEntity<?> book(@RequestBody BookReq r, HttpServletRequest req) {
        if (r.date().isBefore(LocalDate.now())) return err(HttpStatus.BAD_REQUEST, "Pick today or a future date");
        if (r.hour() < 6 || r.hour() > 21) return err(HttpStatus.BAD_REQUEST, "Slots run from 6 AM to 10 PM");
        Venue v = venues.findById(r.venueId()).orElse(null);
        if (v == null) return err(HttpStatus.NOT_FOUND, "Venue not found");
        Booking b = new Booking();
        b.userId = (Long) req.getAttribute("userId"); b.venueId = v.id; b.bookingDate = r.date(); b.slotHour = r.hour();
        b.price = priceFor(v.basePrice, r.hour());
        b.code = "SB-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        b.venueName = v.name; b.emoji = v.emoji;
        try { return ResponseEntity.ok(bookings.saveAndFlush(b)); }
        catch (DataIntegrityViolationException e) { return err(HttpStatus.CONFLICT, "Someone just booked that slot. Pick another one."); }
    }

    @GetMapping("/bookings/my")
    public List<Booking> mine(HttpServletRequest req) { return bookings.findByUserIdOrderByBookingDateDescSlotHourAsc((Long) req.getAttribute("userId")); }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<?> cancel(@PathVariable Long id, HttpServletRequest req) {
        Booking b = bookings.findById(id).orElse(null);
        if (b == null || !b.userId.equals(req.getAttribute("userId"))) return err(HttpStatus.NOT_FOUND, "Booking not found");
        bookings.delete(b);
        return ResponseEntity.ok(Map.of("message", "Cancelled"));
    }

    // Peak hours (6 PM - 9 PM) cost 40% more, early mornings 20% less
    static int priceFor(int base, int hour) { return hour >= 18 && hour <= 20 ? (int) (base * 1.4) : hour < 8 ? (int) (base * 0.8) : base; }
}
