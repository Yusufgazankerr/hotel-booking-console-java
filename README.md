**Status:**  V1 Completed  
**Type:** Portfolio / Learning Project

# Hotel Room Booking Console App (Java + SQLite)

A small but fully structured **hotel room booking system** written in Java, using a clean layered architecture and SQLite for persistence.

The goal of this project is to demonstrate:

- **Domain modelling** (rooms, bookings, privileges)
- **Business rules** (capacity, availability, overlap checks)
- **Separation of concerns** (UI / service / persistence)
- **JDBC + SQLite** usage without heavy frameworks

---

## Features

- Add hotel rooms with:
    - Room number (unique)
    - Room type (enum: e.g. SINGLE, DOUBLE, SUITE)
    - Max guests
    - Features: balcony, beach view, air conditioning
- Create bookings with:
    - Check-in / check-out dates
    - Guest count and list of guest names
    - Board type / guest privilege (e.g. ALL_INCLUSIVE, ULTRA_ALL_INCLUSIVE)
    - Created-by source (ONLINE, FRONT_DESK, AGENCY, etc.)
    - Special requests
- Enforced business rules:
    - No duplicate room numbers
    - Guest count cannot exceed room capacity
    - Guest count must match number of guest names
    - Check-in date must be before check-out date
    - **No overlapping bookings for the same room**
    - Back-to-back bookings (e.g. 10–15 and 15–20) are allowed
- Console-based UI:
    - List all rooms
    - Add new room
    - List bookings for a room
    - Create booking for a room
    - Check availability for a room and date range
    - “Press Enter to continue…” flow for readability

All data is persisted in a local `SQLite` database file (`hotel_booking.db`).

---

## Tech Stack

- **Language:** Java
- **Database:** SQLite (file-based)
- **Persistence:** Plain JDBC
- **Build / IDE:** Maven (optional), IntelliJ IDEA

No Spring, no frameworks – just core Java + JDBC.

---

## Architecture Overview

The project is organized into simple packages:

- `Main` – application entry point
- `java.domain`
    - `HotelRoom`, `HotelBooking`
    - `RoomType`, `GuestPrivilege`
- `java.persistence`
    - `DatabaseManager` – SQLite connection and schema initialization
- `java.persistence.repository`
    - `HotelRoomRepository`, `HotelBookingRepository` – repository interfaces
- `java.persistence.repository.sqlite`
    - `SQLiteHotelRoomRepository`, `SQLiteHotelBookingRepository` – JDBC implementations
- `java.service`
    - `BookingService` – core business logic (validation + availability checks)
- `java.console`
    - `ConsoleApp` – console menu + input handling
    - `ConsoleView` (optional) – helper for formatted console output

### Layered responsibilities

- **Domain (`domain`)**: pure data structures, no DB or UI dependencies.
- **Persistence (`persistence`)**: how data is stored (SQLite via JDBC).
- **Service (`service`)**: business rules and orchestration.
- **Console (`console`)**: user interaction, printing, and reading input.

---

## How Availability & Overlap Rules Work

Booking overlap is calculated using **half-open intervals**: `[checkIn, checkOut)`.

For a given room, two bookings overlap if:

```text
existing.checkIn < new.checkOut
AND
existing.checkOut > new.checkIn
