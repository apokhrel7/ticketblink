# TicketBlink 🎟️

A full-stack seat-based event ticketing platform built with **Spring Boot** and **React**. Designed to handle real transactional complexity — concurrent seat bookings, ACID transactions, and pessimistic locking to prevent double-booking.

NOTE: This is the public release of a project developed privately. The commit history reflects the published state, not the full development history

---

## Architecture Overview

```
┌─────────────┐       ┌──────────────────┐       ┌─────────────┐
│  React SPA  │──────▶│  Spring Boot API │──────▶│   MySQL 8   │
│  (Vite)     │ REST  │  (Port 8080)     │  JPA  │ (Port 3306) │
│  Port 5173  │◀──────│  + JWT Auth      │◀──────│             │
└─────────────┘       └──────────────────┘       └─────────────┘
```

### Tech Stack

| Layer        | Technology                                      |
|:-------------|:------------------------------------------------|
| **Frontend** | React 18, React Router, Tailwind CSS, Axios     |
| **Backend**  | Java 17, Spring Boot 3.2, Spring Security, JPA  |
| **Auth**     | JWT (jjwt) — stateless, 24h expiry              |
| **Database** | MySQL 8 with indexed foreign keys                |
| **Infra**    | Docker Compose (MySQL + Backend + Frontend)      |

### Database Schema

```
users ──────────< orders >──────────< tickets
                    │                    │
                    └────── events ──────┘
                              │
                           venues ──────< seats
```

**Key relationships:**
- A `Venue` has many `Seats` (generated based on rows × seatsPerRow)
- An `Event` belongs to a `Venue` and defines tiered pricing (Standard / Premium / VIP)
- An `Order` belongs to a `User` and an `Event`, and contains multiple `Tickets`
- A `Ticket` links one `Seat` to one `Event` — the `(event_id, seat_id)` pair has a **unique constraint** so the same seat can never be double-booked for the same event

### Concurrency Strategy

The core booking flow uses **pessimistic locking** to prevent race conditions:

```java
// 1. Acquire row-level locks (SELECT ... FOR UPDATE)
List<Seat> seats = seatRepository.findAllByIdForUpdate(seatIds);

// 2. Verify none are already booked
for (Seat seat : seats) {
    if (ticketRepository.existsByEventIdAndSeatIdAndStatus(eventId, seat.getId(), BOOKED))
        throw ApiException.conflict("Seat already booked");
}

// 3. Create order + tickets in a single ACID transaction
Order order = orderRepository.save(newOrder);
```

If two users try to book the same seat simultaneously, one transaction will block on the `FOR UPDATE` lock until the other commits. The second transaction will then see the seat is booked and return a conflict error.

---

## Getting Started

### Prerequisites

- **Docker** and **Docker Compose** (recommended — runs everything)
- OR: **Java 17+**, **Maven**, **Node.js 18+**, **MySQL 8**

### Option A: Docker Compose (Easiest)

```bash
# Clone and start everything
cd ticketblink
docker compose up --build
```

This spins up:
- **MySQL** on port `3306` (auto-creates the `ticketblink` database)
- **Backend API** on port `8080` (waits for MySQL to be healthy)
- **Frontend** on port `5173` (proxies `/api` to backend)

Open **http://localhost:5173** and you're ready to go.

### Option B: Run Locally (Without Docker)

#### 1. Start MySQL

Make sure MySQL 8 is running locally and create the database:

```sql
CREATE DATABASE ticketblink;
CREATE USER 'ticketblink'@'localhost' IDENTIFIED BY 'ticketblink123';
GRANT ALL PRIVILEGES ON ticketblink.* TO 'ticketblink'@'localhost';
FLUSH PRIVILEGES;
```

Or use Docker just for MySQL:

```bash
docker run -d --name ticketblink-db \
  -e MYSQL_ROOT_PASSWORD=rootpass \
  -e MYSQL_DATABASE=ticketblink \
  -e MYSQL_USER=ticketblink \
  -e MYSQL_PASSWORD=ticketblink123 \
  -p 3306:3306 \
  mysql:8.0
```

#### 2. Start the Backend

```bash
cd backend
./mvnw spring-boot:run
# If on Windows: mvnw.cmd spring-boot:run
```

The API starts on **http://localhost:8080**. On first boot, the `DataSeeder` populates sample venues and events.

#### 3. Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

Opens on **http://localhost:5173**. Vite proxies all `/api` calls to `localhost:8080`.

---

## Sample Data

On first startup, the seeder creates:

**3 Venues** (Calgary-based):
- Scotiabank Saddledome (10 rows × 15 seats)
- Arts Commons (8 rows × 12 seats)
- McMahon Stadium (12 rows × 20 seats)

**5 Events** with varied pricing tiers, scheduled 14–75 days out.

Seat types are assigned by row position: front 20% = VIP, next 30% = Premium, rest = Standard.

---

## API Reference

### Auth

| Method | Endpoint             | Body                                  | Auth? | Description     |
|:-------|:---------------------|:--------------------------------------|:------|:----------------|
| POST   | `/api/auth/register` | `{ name, email, password }`           | No    | Create account  |
| POST   | `/api/auth/login`    | `{ email, password }`                 | No    | Get JWT token   |

**Response** (both endpoints):
```json
{
  "token": "eyJhbG...",
  "name": "Anish",
  "email": "anish@example.com",
  "role": "USER"
}
```

### Events (Public)

| Method | Endpoint                 | Auth? | Description                           |
|:-------|:-------------------------|:------|:--------------------------------------|
| GET    | `/api/events`            | No    | List upcoming events                  |
| GET    | `/api/events/all`        | No    | List all events (including past)      |
| GET    | `/api/events/{id}`       | No    | Get single event details              |
| GET    | `/api/events/{id}/seats` | No    | Get seat map with booking status      |

### Bookings (Authenticated)

| Method | Endpoint                              | Body                          | Auth? | Description       |
|:-------|:--------------------------------------|:------------------------------|:------|:------------------|
| POST   | `/api/bookings`                       | `{ eventId, seatIds: [1,2] }` | Yes   | Book seats        |
| GET    | `/api/bookings/orders`                | —                             | Yes   | List my orders    |
| POST   | `/api/bookings/orders/{id}/cancel`    | —                             | Yes   | Cancel an order   |

**Auth header**: `Authorization: Bearer <token>`

---

## Project Structure

```
ticketblink/
├── docker-compose.yml
├── .gitignore
├── README.md
│
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/ticketblink/
│       ├── TicketBlinkApplication.java      # Entry point
│       ├── config/
│       │   ├── SecurityConfig.java          # CORS, JWT filter chain, auth provider
│       │   ├── JwtService.java              # Token generation & validation
│       │   └── JwtAuthFilter.java           # Extracts JWT from Authorization header
│       ├── controller/
│       │   ├── AuthController.java          # POST /auth/register, /auth/login
│       │   ├── EventController.java         # GET /events, /events/{id}/seats
│       │   └── BookingController.java       # POST /bookings, GET /bookings/orders
│       ├── service/
│       │   ├── AuthService.java             # Registration, login, password hashing
│       │   ├── EventService.java            # Event listing, seat availability
│       │   └── BookingService.java          # ACID booking with pessimistic locking
│       ├── entity/
│       │   ├── User.java                    # Implements UserDetails for Spring Security
│       │   ├── Venue.java
│       │   ├── Seat.java
│       │   ├── Event.java
│       │   ├── Order.java
│       │   ├── Ticket.java
│       │   └── enums/                       # Role, SeatType, OrderStatus, TicketStatus
│       ├── repository/                      # Spring Data JPA interfaces
│       ├── dto/                             # Request/Response DTOs (validation annotations)
│       ├── exception/
│       │   ├── ApiException.java            # Custom exception with HTTP status
│       │   └── GlobalExceptionHandler.java  # Centralized error responses
│       └── seeder/
│           └── DataSeeder.java              # Sample data on first boot
│
└── frontend/
    ├── Dockerfile
    ├── package.json
    ├── vite.config.js
    ├── tailwind.config.js
    ├── index.html
    └── src/
        ├── main.jsx                         # React entry point
        ├── App.jsx                          # Routes + ProtectedRoute wrapper
        ├── index.css                        # Tailwind + custom component classes
        ├── api/
        │   ├── client.js                    # Axios instance with JWT interceptor
        │   └── services.js                  # authApi, eventsApi, bookingsApi
        ├── context/
        │   └── AuthContext.jsx              # Auth state + login/register/logout
        ├── components/
        │   ├── Navbar.jsx
        │   ├── EventCard.jsx
        │   └── SeatMap.jsx                  # Interactive seat grid with color-coded types
        └── pages/
            ├── HomePage.jsx                 # Event listing grid
            ├── EventPage.jsx                # Event detail + seat selection + booking
            ├── LoginPage.jsx
            ├── RegisterPage.jsx
            └── OrdersPage.jsx               # Order history with cancel functionality
```

---

## Design Decisions

**Why pessimistic locking over optimistic?**
For a ticketing system, seat conflicts are high-contention and expected. Pessimistic locking (`SELECT ... FOR UPDATE`) prevents conflicts at the DB level rather than retrying on version conflicts. This is simpler to reason about and guarantees a user won't see a "success" that gets rolled back.

**Why a unique constraint on (event_id, seat_id)?**
This is a safety net on top of the application-level check. Even if the pessimistic lock somehow fails (e.g., a bug), the DB will reject a duplicate ticket for the same seat at the same event.

**Why separate Seat and Ticket entities?**
Seats are physical — they belong to a venue and exist regardless of events. Tickets are transactional — they link a seat to a specific event booking. This means the same seat can be booked for different events, and cancelling a ticket frees the seat without touching the venue layout.

**Why JWT over sessions?**
Stateless auth aligns with the resume's stated goals. The token carries the user identity, so the backend doesn't need session storage. The tradeoff is that tokens can't be individually revoked (would need a blacklist), but for a portfolio project this is the right call.

---

## Adding New Features

The codebase is structured for easy extension:

- **New entity**: Add entity → repository → DTO → service → controller. Each layer is independent.
- **New API endpoint**: Add method to existing controller, inject the service you need.
- **Admin panel**: The `Role` enum already supports `ADMIN`. Add `@PreAuthorize("hasRole('ADMIN')")` to admin-only endpoints.
- **Payment integration**: Add a `PaymentService` that's called inside `BookingService.bookSeats()` before creating the order. The transaction will roll back if payment fails.
- **Email notifications**: Add a Spring `@EventListener` on order creation events — keeps it decoupled from the booking flow.
- **Pagination**: Swap `List<Event>` returns for `Page<Event>` using Spring Data's `Pageable` parameter.

---

## Environment Variables

| Variable                      | Default                              | Description              |
|:------------------------------|:-------------------------------------|:-------------------------|
| `SPRING_DATASOURCE_URL`       | `jdbc:mysql://localhost:3306/...`     | MySQL connection string  |
| `SPRING_DATASOURCE_USERNAME`  | `ticketblink`                        | DB username              |
| `SPRING_DATASOURCE_PASSWORD`  | `ticketblink123`                     | DB password              |
| `JWT_SECRET`                  | (base64 encoded default)             | JWT signing key          |

> **Important**: Change `JWT_SECRET` to a unique value in production.

---

## License

Personal project — built for learning and portfolio purposes.
