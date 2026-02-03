package console;

import domain.GuestPrivilege;
import domain.HotelBooking;
import domain.HotelRoom;
import domain.RoomType;
import persistence.repository.HotelBookingRepository;
import persistence.repository.HotelRoomRepository;
import service.BookingService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Console-based user interface for the hotel booking system.
 *
 * Responsible for:
 * - displaying menus
 * - reading user input
 * - calling BookingService and repositories
 * - printing human-friendly output
 *
 * This class does not contain business rules (validation, overlap logic, etc.),
 * which are delegated to BookingService.
 */
public class ConsoleApp {

   private final HotelRoomRepository roomRepository;
   private final HotelBookingRepository bookingRepository;
   private final BookingService bookingService;
   private final Scanner scanner = new Scanner(System.in);

   public ConsoleApp(HotelRoomRepository roomRepository,
                     HotelBookingRepository bookingRepository,
                     BookingService bookingService) {
      this.roomRepository = roomRepository;
      this.bookingRepository = bookingRepository;
      this.bookingService = bookingService;
   }

   /**
    * Main loop of the console application.
    * Repeatedly displays the menu, reads the user's choice,
    * and dispatches to the appropriate handler until the user chooses to exit.
    */
   public void run() {
      boolean running = true;

      while (running) {
         printMenu();
         String choice = scanner.nextLine().trim();

         switch (choice) {
            case "1" -> listRooms();
            case "2" -> addRoom();
            case "3" -> listBookingsForRoom();
            case "4" -> createBooking();
            case "5" -> checkAvailability();
            case "0" -> {
               System.out.println("Exiting...");
               running = false;
            }
            default -> System.out.println("Unknown option. Please try again");
         }

         System.out.println();
      }
   }

   private void printMenu() {
      System.out.println("==== Hotel Booking Console ====");
      System.out.println("1) List all rooms");
      System.out.println("2) Add a new room");
      System.out.println("3) List bookings for a room");
      System.out.println("4) Create a new booking");
      System.out.println("5) Check room availability");
      System.out.println("0) Exit");
      System.out.print("Choose an option: ");
   }

   private void listRooms() {
      var rooms = roomRepository.findAll();
      if (rooms.isEmpty()) {
         System.out.println("No rooms found.");
         waitForEnter();
         return;
      }

      System.out.println("Rooms:");
      for (HotelRoom room : rooms) {
         printRoom(room);
         System.out.println();
      }

      waitForEnter();
   }

   private void addRoom() {
      try {
         System.out.print("Enter room number (e.g. 101): ");
         int roomNumber = Integer.parseInt(scanner.nextLine().trim());

         // 🔴 NEW: check for existing room with same number
         var existing = roomRepository.findByRoomNumber(roomNumber);
         if (existing.isPresent()) {
            System.out.println("A room with number " + roomNumber + " already exists (ID="
                    + existing.get().getId() + "). Room NOT created.");
            waitForEnter();
            return;
         }

         System.out.println("Select room type:");
         RoomType[] types = RoomType.values();
         for (int i = 0; i < types.length; i++) {
            System.out.println("  " + (i + 1) + ") " + types[i]);
         }
         System.out.print("Choice: ");
         int typeChoice = Integer.parseInt(scanner.nextLine().trim());
         if (typeChoice < 1 || typeChoice > types.length) {
            System.out.println("Invalid room type choice.");
            return;
         }
         RoomType roomType = types[typeChoice - 1];

         System.out.print("Enter max guests: ");
         int maxGuests = Integer.parseInt(scanner.nextLine().trim());

         System.out.print("Has balcony? (y/n): ");
         boolean hasBalcony = yesNoInput();

         System.out.print("Has beach view? (y/n): ");
         boolean hasBeachView = yesNoInput();

         System.out.print("Has air conditioning? (y/n): ");
         boolean hasAC = yesNoInput();

         HotelRoom room = new HotelRoom(
                 roomNumber,
                 roomType,
                 maxGuests,
                 hasBalcony,
                 hasBeachView,
                 hasAC
         );

         roomRepository.save(room);
         System.out.println("Room successfully created.");
         printRoom(room);

      } catch (NumberFormatException e) {
         System.out.println("Invalid numeric input. Room not created");
      }

      waitForEnter();
   }

   private boolean yesNoInput() {
      String line = scanner.nextLine().trim().toLowerCase();
      return line.startsWith("y");
   }

   private void listBookingsForRoom() {
      try {
         System.out.print("Enter room number: ");
         int roomNumber = Integer.parseInt(scanner.nextLine().trim());

         var roomOpt = roomRepository.findByRoomNumber(roomNumber);
         if (roomOpt.isEmpty()) {
            System.out.println("No room found with number " + roomNumber);
            return;
         }

         HotelRoom room = roomOpt.get();
         var bookings = bookingRepository.findByRoomId(room.getId());

         if (bookings.isEmpty()) {
            System.out.println("No bookings for room " + roomNumber);
            return;
         }

         System.out.println("Bookings for room " + roomNumber + ":");
         for (HotelBooking b : bookings) {
            printBooking(b);
            System.out.println();
         }
      } catch (NumberFormatException e) {
         System.out.println("Invalid number.");
      }
   }

   private void createBooking() {
      try {
         System.out.print("Enter room number: ");
         int roomNumber = Integer.parseInt(scanner.nextLine().trim());

         var roomOpt = roomRepository.findByRoomNumber(roomNumber);
         if (roomOpt.isEmpty()) {
            System.out.println("No room found with number " + roomNumber);
            return;
         }
         HotelRoom room = roomOpt.get();

         LocalDate checkIn = readDate("Enter check-in date (YYYY-MM-DD): ");
         if (checkIn == null) return;

         LocalDate checkOut = readDate("Enter check-out date (YYYY-MM-DD): ");
         if (checkOut == null) return;

         System.out.print("Enter guest count: ");
         int guestCount = Integer.parseInt(scanner.nextLine().trim());

         List<String> guestNames = new ArrayList<>();
         for (int i = 1; i <= guestCount; i++) {
            System.out.print("Enter name of guest " + i + ": ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
               System.out.println("Name cannot be empty. Booking cancelled.");
               return;
            }
            guestNames.add(name);
         }

         System.out.print("Enter createdBy (e.g. FRONT_DESK, ONLINE, AGENCY: XYZ): ");
         String createdBy = scanner.nextLine().trim();

         System.out.println("Select guest privilege:");
         GuestPrivilege[] privs = GuestPrivilege.values();
         for (int i = 0; i < privs.length; i++) {
            System.out.println("  " + (i + 1) + ") " + privs[i]);
         }
         System.out.print("Choice: ");
         int privChoice = Integer.parseInt(scanner.nextLine().trim());
         if (privChoice < 1 || privChoice > privs.length) {
            System.out.println("Invalid privilege choice.");
            return;
         }
         GuestPrivilege privilege = privs[privChoice - 1];

         System.out.print("Special requests (or leave empty): ");
         String specialRequests = scanner.nextLine().trim();

         try {
            HotelBooking booking = bookingService.createBooking(
                    room.getId(),
                    checkIn,
                    checkOut,
                    guestCount,
                    guestNames,
                    createdBy,
                    privilege,
                    specialRequests
            );

            System.out.println("Booking created: " + booking);

         } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Could not create booking: " + e.getMessage());
         }

         waitForEnter();

      } catch (NumberFormatException e) {
         System.out.println("Invalid numeric input. Booking not created.");
      }
   }

   private LocalDate readDate(String prompt) {
      System.out.print(prompt);
      String text = scanner.nextLine().trim();
      try {
         return LocalDate.parse(text);
      } catch (DateTimeParseException e) {
         System.out.println("Invalid date format. Expected YYYY-MM-DD. Operation cancelled.");
         return null;
      }
   }

   private void checkAvailability() {
      try {
         System.out.print("Enter room number: ");
         int roomNumber = Integer.parseInt(scanner.nextLine().trim());

         var roomOpt = roomRepository.findByRoomNumber(roomNumber);
         if (roomOpt.isEmpty()) {
            System.out.println("No room found with number " + roomNumber);
            return;
         }
         HotelRoom room = roomOpt.get();

         LocalDate checkIn = readDate("Enter check-in date (YYYY-MM-DD): ");
         if (checkIn == null) return;

         LocalDate checkOut = readDate("Enter check-out date (YYYY-MM-DD): ");
         if (checkOut == null) return;

         try {
            boolean available = bookingService.isRoomAvailable(
                    room.getId(),
                    checkIn,
                    checkOut
            );

            if (available) {
               System.out.println("Room " + roomNumber + " IS available between " +
                       checkIn + " and " + checkOut + ".");
            } else {
               System.out.println("Room " + roomNumber + " is NOT available between " +
                       checkIn + " and " + checkOut + ".");
            }
         } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
         }

         waitForEnter();

      } catch (NumberFormatException e) {
         System.out.println("Invalid number.");
      }
   }

   /**
    * Prints a human-friendly summary of a hotel room to the console.
    */
   private void printRoom(HotelRoom room) {
      System.out.println("Room " + room.getRoomNumber() + " (" + room.getRoomType() + ")");
      System.out.println("  • Capacity: " + room.getMaxGuests() + " guests");
      System.out.println("  • Balcony: " + yesNo(room.hasBalcony()));
      System.out.println("  • Beach view: " + yesNo(room.hasBeachView()));
      System.out.println("  • Air conditioning: " + yesNo(room.hasAirConditioning()));
   }

   /**
    * Prints a human-friendly summary of a booking to the console.
    */
   private void printBooking(HotelBooking b) {
      System.out.println("Booking #" + b.getId());
      System.out.println("  • Stay: " + b.getCheckInDate() + " → " + b.getCheckOutDate());
      System.out.println("  • Guests (" + b.getGuestCount() + "): " + String.join(", ", b.getGuestNames()));
      System.out.println("  • Privilege: " + prettifyEnum(b.getGuestPrivilege()));
      System.out.println("  • Created by: " + b.getCreatedBy());

      if (b.getSpecialRequests() != null && !b.getSpecialRequests().isBlank()) {
         System.out.println("  • Special requests: " + b.getSpecialRequests());
      }
   }

   private String yesNo(boolean value) {
      return value ? "Yes" : "No";
   }

   private String prettifyEnum(Enum<?> e) {
      return e.name().replace("_", " ");
   }

   /**
    * Simple "press Enter to continue" pause between screens.
    * Provides time for the user to read the output before returning to the menu.
    */
   private void waitForEnter() {
      System.out.println();
      System.out.print("Press Enter to continue...");
      scanner.nextLine();  // just wait for user to hit Enter
      System.out.println();
   }
}
