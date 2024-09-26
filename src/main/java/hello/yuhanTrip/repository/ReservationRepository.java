package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.domain.reservation.Reservation;
import hello.yuhanTrip.domain.reservation.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByRoomId(Long roomId);


    @Modifying
    @Query("UPDATE Reservation r SET r.reservationStatus = :status WHERE r.id = :id")
    void updateReservationStatus(@Param("id") Long id, @Param("status") ReservationStatus status);


    @Query("SELECT r FROM Reservation r WHERE r.room.id = :roomId AND " +
            "(r.checkInDate <= :checkOutDate AND r.checkOutDate >= :checkInDate) AND " +
            "r.reservationStatus NOT IN :excludedStatuses")
    List<Reservation> findOverlappingReservations(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("excludedStatuses") List<ReservationStatus> excludedStatuses
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

    @Query("SELECT r FROM Reservation r WHERE r.member = :member AND r.reservationStatus != :status")
    Page<Reservation> findByMember(@Param("member") Member member, @Param("status") ReservationStatus status, Pageable pageable);

    List<Reservation> findByReservationStatusAndCheckOutDateBefore(ReservationStatus status, LocalDate date);

}