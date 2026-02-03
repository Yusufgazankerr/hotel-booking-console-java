package persistence.repository.sqlite;

import domain.GuestPrivilege;
import domain.HotelBooking;
import persistence.DatabaseManager;
import persistence.repository.HotelBookingRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteHotelBookingRepository implements HotelBookingRepository {

   @Override
   public HotelBooking save(HotelBooking booking) {

      if (booking.getId() != null) {
         // For now, we only support inserting new bookings
         throw new UnsupportedOperationException("Updating bookings is not implemented yet");
      }

      String sql = """
                INSERT INTO hotel_bookings
                (room_id, check_in_date, check_out_date, guest_count,
                 guest_names, created_by, guest_privilege,
                 special_requests, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

         ps.setInt(1, booking.getRoomId());
         ps.setString(2, booking.getCheckInDate().toString());   // LocalDate -> "YYYY-MM-DD"
         ps.setString(3, booking.getCheckOutDate().toString());
         ps.setInt(4, booking.getGuestCount());
         ps.setString(5, serializeGuestNames(booking.getGuestNames()));
         ps.setString(6, booking.getCreatedBy());
         ps.setString(7, booking.getGuestPrivilege().name());
         ps.setString(8, booking.getSpecialRequests());
         ps.setString(9, booking.getCreatedAt().toString());     // LocalDateTime -> ISO string

         ps.executeUpdate();

         try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
               booking.setId(rs.getInt(1));
            }
         }

         return booking;

      } catch (SQLException e) {
         throw new RuntimeException("Failed to insert booking", e);
      }
   }

   @Override
   public Optional<HotelBooking> findById(int id) {
      String sql = """
            SELECT *
            FROM hotel_bookings
            WHERE id = ?
            """;

      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql)) {

         ps.setInt(1, id);

         try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
               return Optional.of(mapRow(rs));
            } else {
               return Optional.empty();
            }
         }

      } catch (SQLException e) {
         throw new RuntimeException("Failed to find booking by id=" + id, e);
      }
   }

   @Override
   public List<HotelBooking> findAll() {
      String sql = """
            SELECT *
            FROM hotel_bookings
            ORDER BY check_in_date
            """;

      List<HotelBooking> result = new ArrayList<>();

      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql);
           ResultSet rs = ps.executeQuery()) {

         while (rs.next()) {
            result.add(mapRow(rs));
         }

         return result;

      } catch (SQLException e) {
         throw new RuntimeException("Failed to list all bookings", e);
      }
   }

   @Override
   public List<HotelBooking> findByRoomId(int roomId) {
      String sql = """
            SELECT *
            FROM hotel_bookings
            WHERE room_id = ?
            ORDER BY check_in_date
            """;

      List<HotelBooking> result = new ArrayList<>();

      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql)) {

         ps.setInt(1, roomId);

         try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
               result.add(mapRow(rs));
            }
         }

         return result;

      } catch (SQLException e) {
         throw new RuntimeException(
                 "Failed to list bookings for roomId=" + roomId, e
         );
      }
   }

   /**
    * Serializes a list of guest names to a single string for storage in the database.
    * Example: ["Alice Smith", "Bob Smith"] -> "Alice Smith|Bob Smith".
    */
   private String serializeGuestNames(List<String> names) {
      // "Alice Smith|Bob Smith"
      return String.join("|", names);
   }

   /**
    * Maps a single ResultSet row to a HotelRoom domain object.
    * Assumes the ResultSet is positioned on a valid row.
    */
   private HotelBooking mapRow(ResultSet rs) throws SQLException {
      int id = rs.getInt("id");
      int roomId = rs.getInt("room_id");

      LocalDate checkIn = LocalDate.parse(rs.getString("check_in_date"));
      LocalDate checkOut = LocalDate.parse(rs.getString("check_out_date"));

      int guestCount = rs.getInt("guest_count");
      List<String> guestNames = deserializeGuestNames(rs.getString("guest_names"));

      String createdBy = rs.getString("created_by");
      GuestPrivilege privilege =
              GuestPrivilege.valueOf(rs.getString("guest_privilege"));

      String specialRequests = rs.getString("special_requests");
      LocalDateTime createdAt =
              LocalDateTime.parse(rs.getString("created_at"));

      return new HotelBooking(
              id,
              roomId,
              checkIn,
              checkOut,
              guestCount,
              guestNames,
              privilege,
              createdBy,
              specialRequests,
              createdAt
      );
   }

   /**
    * Deserializes the guest_names string from the database back into a list.
    * Example: "Alice Smith|Bob Smith" -> ["Alice Smith", "Bob Smith"].
    */
   private List<String> deserializeGuestNames(String text) {
      if (text == null || text.isBlank()) {
         return List.of();
      }
      return List.of(text.split("\\|"));
   }
}
