package persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;   // when needed

/**
 * Centralized access for SQLite connections and schema initialization.
 */
public class DatabaseManager {

   // This will create/use a file called hotel_booking.db in your project folder
   private static final String DB_URL = "jdbc:sqlite:hotel_booking.db";

   /**
    * Opens a new SQLite connection to the application database file.
    *
    * @return a new JDBC connection
    * @throws SQLException if the connection cannot be opened
    */
   public static Connection getConnection() throws SQLException {
      return DriverManager.getConnection(DB_URL);
   }

   // NEW: create tables (for now only hotel_rooms)
   /**
    * Creates required tables if they do not already exist.
    *
    * <p>Intended to be called once at application startup.</p>
    *
    * @throws RuntimeException if schema creation fails
    */
   public static void initializeSchema() {
      String sql = """
              CREATE TABLE IF NOT EXISTS hotel_rooms (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  room_number INTEGER NOT NULL UNIQUE,
                  room_type TEXT NOT NULL,
                  max_guests INTEGER NOT NULL CHECK (max_guests > 0),
                  has_balcony INTEGER NOT NULL CHECK (has_balcony IN (0, 1)),
                  has_beach_view INTEGER NOT NULL CHECK (has_beach_view IN (0, 1)),
                  has_air_conditioning INTEGER NOT NULL CHECK (has_air_conditioning IN (0, 1))
              );
              
              CREATE TABLE IF NOT EXISTS hotel_bookings (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  room_id INTEGER NOT NULL,
                  check_in_date TEXT NOT NULL,
                  check_out_date TEXT NOT NULL,
                  guest_count INTEGER NOT NULL CHECK (guest_count > 0),
                  guest_names TEXT NOT NULL,
                  created_by TEXT NOT NULL,
                  guest_privilege TEXT NOT NULL,
                  special_requests TEXT,
                  created_at TEXT NOT NULL,
                  FOREIGN KEY (room_id) REFERENCES hotel_rooms(id) ON DELETE CASCADE
              );
              """;

      try (Connection conn = getConnection();
           Statement stmt = conn.createStatement()) {

         stmt.executeUpdate(sql);

      } catch (SQLException e) {
         throw new RuntimeException("Failed to initialize database schema", e);
      }
   }
}
