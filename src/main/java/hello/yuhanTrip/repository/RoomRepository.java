package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.reservation.ReservationStatus;
import hello.yuhanTrip.domain.accommodation.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room,Long> {



    @Query("SELECT r FROM Room r WHERE r.accommodation.id = :accommodationId AND r.id NOT IN (" +
            "SELECT r.id FROM Reservation res " +
            "WHERE res.room.id = r.id " +
            "AND (res.checkInDate < :checkOutDate AND res.checkOutDate > :checkInDate) " +
            "AND res.reservationStatus NOT IN :excludedStatuses)")
    List<Room> findAvailableRoomsByAccommodation(
            @Param("accommodationId") Long accommodationId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("excludedStatuses") List<ReservationStatus> excludedStatuses
    );


}
