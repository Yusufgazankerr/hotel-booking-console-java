package domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class HotelBooking {

   private Integer id;
   private int roomId;

   private LocalDate checkInDate;
   private LocalDate checkOutDate;

   private int guestCount;
   private List<String> guestNames;

   private String createdBy;
   private GuestPrivilege guestPrivilege;
   private String specialRequests;

   private LocalDateTime createdAt;

   // Full constructor
   public HotelBooking(Integer id,
                       int roomId,
                       LocalDate checkInDate,
                       LocalDate checkOutDate,
                       int guestCount,
                       List<String> guestNames,
                       GuestPrivilege guestPrivilege,
                       String createdBy,
                       String specialRequests,
                       LocalDateTime createdAt) {
      this.id = id;
      this.roomId = roomId;
      this.checkInDate = checkInDate;
      this.checkOutDate = checkOutDate;
      this.guestCount = guestCount;
      this.guestNames = guestNames;
      this.guestPrivilege = guestPrivilege;
      this.createdBy = createdBy;
      this.specialRequests = specialRequests;
      this.createdAt = createdAt;
   }

   // Constructor for new bookings created in code
   public HotelBooking(int roomId,
                       LocalDate checkInDate,
                       LocalDate checkOutDate,
                       int guestCount,
                       List<String> guestNames,
                       GuestPrivilege guestPrivilege,
                       String createdBy,
                       String specialRequests) {
      this(null,
              roomId,
              checkInDate,
              checkOutDate,
              guestCount,
              guestNames,
              guestPrivilege,
              createdBy,
              specialRequests,
              LocalDateTime.now());
   }

   public Integer getId() {
      return id;
   }

   public int getRoomId() {
      return roomId;
   }

   public LocalDate getCheckInDate() {
      return checkInDate;
   }

   public LocalDate getCheckOutDate() {
      return checkOutDate;
   }

   public int getGuestCount() {
      return guestCount;
   }

   public List<String> getGuestNames() {
      return guestNames;
   }

   public String getCreatedBy() {
      return createdBy;
   }

   public GuestPrivilege getGuestPrivilege() {
      return guestPrivilege;
   }

   public String getSpecialRequests() {
      return specialRequests;
   }

   public LocalDateTime getCreatedAt() {
      return createdAt;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   @Override
   public String toString() {
      return "domain.HotelBooking{" +
              "id=" + id +
              ", roomId=" + roomId +
              ", checkInDate=" + checkInDate +
              ", checkOutDate=" + checkOutDate +
              ", guestCount=" + guestCount +
              ", guestNames=" + guestNames +
              ", createdBy='" + createdBy + '\'' +
              ", guestPrivilege=" + guestPrivilege +
              ", specialRequests='" + specialRequests + '\'' +
              ", createdAt=" + createdAt +
              '}';
   }
}
