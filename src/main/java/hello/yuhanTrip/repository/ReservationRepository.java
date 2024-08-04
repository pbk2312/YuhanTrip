package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface ReservationRepository extends JpaRepository<Reservation, Long> {


    @Query("SELECT r FROM Reservation r WHERE r.room.id = :roomId AND " +
            "(r.checkInDate <= :checkOutDate AND r.checkOutDate >= :checkInDate)")
    List<Reservation> findOverlappingReservations(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

    @Query("select o from Reservation o" +
            " left join fetch o.payment p" +
            " left join fetch o.member m" +
            " where o.reservationUid = :reservationUid")
    Optional<Reservation> findReservationAndPaymentAndMember(@Param("reservationUid") String reservationUid);

    @Query("select o from Reservation o" +
            " left join fetch o.payment p" +
            " where o.reservationUid = :reservationUid")
    Optional<Reservation> findReservationAndPayment(@Param("reservationUid") String reservationUid);

    @Query("select o from Reservation o where o.reservationUid = :reservationUid")
    Optional<Reservation> findByReservationUid(@Param("reservationUid") String reservationUid);

}