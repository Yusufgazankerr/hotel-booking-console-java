package service;

import domain.GuestPrivilege;
import domain.HotelBooking;
import domain.HotelRoom;
import persistence.repository.HotelBookingRepository;
import persistence.repository.HotelRoomRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Domain service for booking creation and availability checks.
 */
public class BookingService {

   private final HotelRoomRepository roomRepository;
   private final HotelBookingRepository bookingRepository;

   public BookingService(HotelRoomRepository roomRepository,
                         HotelBookingRepository bookingRepository) {
      this.roomRepository = roomRepository;
      this.bookingRepository = bookingRepository;
   }

   /**
    * Checks whether a room has no conflicting bookings for the given date range.
    *
    * <p>Dates are treated as a half-open interval: [checkIn, checkOut).</p>
    *
    * @param roomId the room identifier
    * @param checkIn the start date (inclusive)
    * @param checkOut the end date (exclusive)
    * @return true if no existing booking overlaps the requested range
    * @throws IllegalArgumentException if dates are null or invalid
    */
   public boolean isRoomAvailable(int roomId,
                                  LocalDate checkIn,
                                  LocalDate checkOut) {
      validateDates(checkIn, checkOut);

      List<HotelBooking> existing = bookingRepository.findByRoomId(roomId);
      for (HotelBooking b : existing) {
         if (datesOverlap(b.getCheckInDate(), b.getCheckOutDate(),
                 checkIn, checkOut)) {
            return false;
         }
      }
      return true;
   }

   /**
    * Validates input and creates a booking if the room exists and is available.
    *
    * @param roomId the room identifier
    * @param checkIn the start date (inclusive)
    * @param checkOut the end date (exclusive)
    * @param guestCount total number of guests
    * @param guestNames list of guest names (size must match guestCount)
    * @param createdBy source of booking (e.g., FRONT_DESK, ONLINE)
    * @param privilege guest privilege tier
    * @param specialRequests optional special requests text
    * @return the saved booking with generated id
    * @throws IllegalArgumentException for invalid input or missing room
    * @throws IllegalStateException if the room is not available
    */
   public HotelBooking createBooking(int roomId,
                                     LocalDate checkIn,
                                     LocalDate checkOut,
                                     int guestCount,
                                     List<String> guestNames,
                                     String createdBy,
                                     GuestPrivilege privilege,
                                     String specialRequests) {

      // 1) Basic null/date validation
      validateDates(checkIn, checkOut);

      if (guestCount <= 0) {
         throw new IllegalArgumentException("guestCount must be positive");
      }

      if (guestNames == null || guestNames.isEmpty()) {
         throw new IllegalArgumentException("guestNames must not be empty");
      }

      if (guestNames.size() != guestCount) {
         throw new IllegalArgumentException(
                 "guestCount (" + guestCount + ") does not match guestNames size (" + guestNames.size() + ")"
         );
      }

      if (createdBy == null || createdBy.isBlank()) {
         throw new IllegalArgumentException("createdBy must not be blank");
      }

      Objects.requireNonNull(privilege, "guestPrivilege must not be null");

      // 2) Check that room exists
      HotelRoom room = roomRepository.findById(roomId)
              .orElseThrow(() -> new IllegalArgumentException("Room with id " + roomId + " does not exist"));

      // 3) Capacity validation
      if (guestCount > room.getMaxGuests()) {
         throw new IllegalArgumentException(
                 "guestCount " + guestCount + " exceeds room capacity " + room.getMaxGuests()
         );
      }

      // 4) Availability validation
      if (!isRoomAvailable(roomId, checkIn, checkOut)) {
         throw new IllegalStateException(
                 "Room " + room.getRoomNumber() + " is not available between " +
                         checkIn + " and " + checkOut
         );
      }

      // 5) All good → create and save booking
      HotelBooking booking = new HotelBooking(
              roomId,
              checkIn,
              checkOut,
              guestCount,
              guestNames,
              privilege,
              createdBy,
              specialRequests
      );

      return bookingRepository.save(booking);
   }

   // ----- private helpers -----

   private void validateDates(LocalDate checkIn, LocalDate checkOut) {
      if (checkIn == null || checkOut == null) {
         throw new IllegalArgumentException("checkIn and checkOut must not be null");
      }
      if (!checkIn.isBefore(checkOut)) {
         throw new IllegalArgumentException(
                 "checkIn (" + checkIn + ") must be before checkOut (" + checkOut + ")"
         );
      }
   }

   // Overlap logic for [existingStart, existingEnd) and [newStart, newEnd)
   private boolean datesOverlap(LocalDate existingStart,
                                LocalDate existingEnd,
                                LocalDate newStart,
                                LocalDate newEnd) {
      return existingStart.isBefore(newEnd) && existingEnd.isAfter(newStart);
   }
}
