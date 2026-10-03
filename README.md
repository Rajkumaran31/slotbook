# SlotBook: book a pitch in 3 taps
Venue slot booking app. React + Spring Boot + MySQL. Peak hours cost 40% more, past and taken slots are disabled, and double bookings are blocked by a MySQL UNIQUE constraint.

## Run it
Requirements: Java 17+, Maven, Node 18+, MySQL running locally.

1. **Backend**: open `backend/src/main/resources/application.properties` and set your MySQL username and password. The `slotbook` database is created automatically, and 6 sample venues are seeded on first start.
   ```
   cd backend
   mvn spring-boot:run
   ```
   API runs at http://localhost:8080
2. **Frontend** (new terminal):
   ```
   cd frontend
   npm install
   npm run dev
   ```
   Open http://localhost:5173

## How it works
| Step | What happens |
|---|---|
| Register / login | Password is hashed with BCrypt, server returns a JWT, React stores it |
| Browse | `GET /api/venues` |
| Pick date | `GET /api/venues/{id}/slots?date=` returns taken hours, which are greyed out |
| Book | `POST /api/bookings` (JWT checked by `AuthInterceptor`). Price is computed on the server. |
| Double booking | `UNIQUE(venueId, bookingDate, slotHour)` makes MySQL reject the second insert. The API answers 409 "Someone just booked that slot." |
| Tickets | `GET /api/bookings/my`, `DELETE /api/bookings/{id}` |

## Structure
```
backend/src/main/java/com/slotbook/
  SlotBookApplication.java   main class + seed data
  model/  User, Venue, Booking
  repo/   Spring Data JPA repositories
  security/ JwtUtil, AuthInterceptor
  web/    ApiController, WebConfig (CORS + auth rules)
frontend/src/ App.jsx, api.js, styles.css
```

## Future scope
Payments (Razorpay), email confirmation, owner dashboard, reviews.
