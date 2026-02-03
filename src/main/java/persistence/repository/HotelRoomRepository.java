package persistence.repository;

import domain.HotelRoom;

import java.util.List;
import java.util.Optional;

public interface HotelRoomRepository {

   HotelRoom save(HotelRoom room); // create or update

   Optional<HotelRoom> findById(int id);

   Optional<HotelRoom> findByRoomNumber(int roomNumber);

   List<HotelRoom> findAll();
}
