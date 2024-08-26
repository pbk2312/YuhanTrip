package hello.yuhanTrip.service.Accomodation;

import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.domain.Reservation;
import hello.yuhanTrip.domain.Room;
import hello.yuhanTrip.dto.ReservationDTO;
import hello.yuhanTrip.dto.ReservationUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {


    void reservationRegister(Reservation reservation);

    void updateReservationStatus(Reservation reservation);

    Reservation findReservation(Long id);

    boolean isDateOverlapping(Long roomId, LocalDate checkInDate, LocalDate checkOutDate);

    void removeReservation(String reservationUid);

    List<Reservation> getReservationsByRoomId(Long roomId);

    Reservation updateReservation(ReservationUpdateDTO reservationDTO, String username);

    void validateReservationDates(ReservationDTO reservationDTO, Room room);

    Page<Reservation> getReservationsByPage(Member member, Pageable pageable);

    boolean cancelReservation(String reservationUid);
}
