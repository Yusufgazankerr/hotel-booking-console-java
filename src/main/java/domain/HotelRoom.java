package domain;

public class HotelRoom {

   private Integer id; // null before it is saved anywhere
   private int roomNumber;
   private RoomType roomType;
   private int maxGuests;

   private boolean hasBalcony;
   private boolean hasBeachView;
   private boolean hasAirConditioning;

   public HotelRoom(Integer id,
                    int roomNumber,
                    RoomType roomType,
                    int maxGuests,
                    boolean hasBalcony,
                    boolean hasBeachView,
                    boolean hasAirConditioning) {
      this.id = id;
      this.roomNumber = roomNumber;
      this.roomType = roomType;
      this.maxGuests = maxGuests;
      this.hasBalcony = hasBalcony;
      this.hasBeachView = hasBeachView;
      this.hasAirConditioning = hasAirConditioning;
   }

   // Constructor for new rooms (id not known yet)
   public HotelRoom(int roomNumber,
                    RoomType roomType,
                    int maxGuests,
                    boolean hasBalcony,
                    boolean hasBeachView,
                    boolean hasAirConditioning) {
      this(null, roomNumber, roomType, maxGuests,
              hasBalcony, hasBeachView, hasAirConditioning);
   }

   public Integer getId() {
      return id;
   }

   public int getRoomNumber() {
      return roomNumber;
   }

   public RoomType getRoomType() {
      return roomType;
   }

   public int getMaxGuests() {
      return maxGuests;
   }

   public boolean hasBalcony() {
      return hasBalcony;
   }

   public boolean hasBeachView() {
      return hasBeachView;
   }

   public boolean hasAirConditioning() {
      return hasAirConditioning;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   @Override
   public String toString() {
      return "domain.HotelRoom{" +
              "id=" + id +
              ", roomNumber=" + roomNumber +
              ", roomType=" + roomType +
              ", maxGuests=" + maxGuests +
              ", hasBalcony=" + hasBalcony +
              ", hasBeachView=" + hasBeachView +
              ", hasAirConditioning=" + hasAirConditioning +
              '}';
   }
}
