package persistence.repository;

import domain.HotelBooking;

import java.util.List;
import java.util.Optional;

public interface HotelBookingRepository {

   HotelBooking save(HotelBooking booking);   // create or update (though we’ll mostly create)

   Optional<HotelBooking> findById(int id);

   List<HotelBooking> findAll();

   List<HotelBooking> findByRoomId(int roomId);
}
