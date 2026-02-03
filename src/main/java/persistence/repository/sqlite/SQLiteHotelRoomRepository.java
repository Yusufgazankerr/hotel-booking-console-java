package persistence.repository.sqlite;

import domain.HotelRoom;
import domain.RoomType;
import persistence.DatabaseManager;
import persistence.repository.HotelRoomRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteHotelRoomRepository implements HotelRoomRepository {

   @Override
   public HotelRoom save(HotelRoom room) {
      if (room.getId() != null) {
         throw new UnsupportedOperationException("Updating rooms is not implemented yet");
      }

      String sql = """
              INSERT INTO hotel_rooms
              (room_number, room_type, max_guests, has_balcony, has_beach_view, has_air_conditioning)
              VALUES (?, ?, ?, ?, ?, ?)
              """;

      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

         ps.setInt(1, room.getRoomNumber());
         ps.setString(2, room.getRoomType().name());
         ps.setInt(3, room.getMaxGuests());
         ps.setInt(4, room.hasBalcony() ? 1 : 0);
         ps.setInt(5, room.hasBeachView() ? 1 : 0);
         ps.setInt(6, room.hasAirConditioning() ? 1 : 0);

         ps.executeUpdate();

         try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
               room.setId(rs.getInt(1));
            }
         }

         return room;

      } catch (SQLException e) {
         throw new RuntimeException("Failed to insert room", e);
      }
   }

   @Override
   public Optional<HotelRoom> findById(int id) {
      String sql = """
                SELECT id, room_number, room_type, max_guests,
                       has_balcony, has_beach_view, has_air_conditioning
                FROM hotel_rooms
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
         throw new RuntimeException("Failed to find room by id", e);
      }
   }

   @Override
   public Optional<HotelRoom> findByRoomNumber(int roomNumber) {
      String sql = """
                SELECT id, room_number, room_type, max_guests,
                       has_balcony, has_beach_view, has_air_conditioning
                FROM hotel_rooms
                WHERE room_number = ?
                """;

      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql)) {

         ps.setInt(1, roomNumber);

         try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
               return Optional.of(mapRow(rs));
            } else {
               return Optional.empty();
            }
         }

      } catch (SQLException e) {
         throw new RuntimeException("Failed to find room by room number", e);
      }
   }

   @Override
   public List<HotelRoom> findAll() {
      String sql = """
              SELECT id, room_number, room_type, max_guests,
                     has_balcony, has_beach_view, has_air_conditioning
              FROM hotel_rooms
              ORDER BY room_number       
              """;

      List<HotelRoom> result = new ArrayList<>();

      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql);
           ResultSet rs = ps.executeQuery()){

         while (rs.next()) {
            result.add(mapRow(rs));
         }

         return result;

      } catch (SQLException e) {
         throw new RuntimeException("Failed to list rooms", e);
      }
   }

   /**
    * Maps a single ResultSet row to a HotelRoom domain object.
    * Assumes the ResultSet is positioned on a valid row.
    */
   private HotelRoom mapRow(ResultSet rs) throws SQLException {
      int id = rs.getInt("id");
      int roomNumber = rs.getInt("room_number");
      String roomTypeText = rs.getString("room_type");
      int maxGuests = rs.getInt("max_guests");
      boolean hasBalcony = rs.getInt("has_balcony") == 1;
      boolean hasBeachView = rs.getInt("has_beach_view") == 1;
      boolean hasAirConditioning = rs.getInt("has_air_conditioning") == 1;

      RoomType roomType = RoomType.valueOf(roomTypeText);

      HotelRoom room = new HotelRoom(
              roomNumber,
              roomType,
              maxGuests,
              hasBalcony,
              hasBeachView,
              hasAirConditioning
      );
      room.setId(id);
      return room;
   }
}
