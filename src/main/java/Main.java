import console.ConsoleApp;
import persistence.DatabaseManager;
import persistence.repository.HotelBookingRepository;
import persistence.repository.HotelRoomRepository;
import persistence.repository.sqlite.SQLiteHotelBookingRepository;
import persistence.repository.sqlite.SQLiteHotelRoomRepository;
import service.BookingService;

public class Main {

   public static void main(String[] args) {

      DatabaseManager.initializeSchema();

      HotelRoomRepository roomRepo = new SQLiteHotelRoomRepository();
      HotelBookingRepository bookingRepo = new SQLiteHotelBookingRepository();

      BookingService bookingService = new BookingService(roomRepo, bookingRepo);
      ConsoleApp app = new ConsoleApp(roomRepo, bookingRepo, bookingService);
      app.run();
   }
}
