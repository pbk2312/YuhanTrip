package hello.yuhanTrip.service.reservation;

import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.domain.reservation.Reservation;
import hello.yuhanTrip.domain.accommodation.Room;
import hello.yuhanTrip.dto.accommodation.ReservationDTO;
import hello.yuhanTrip.dto.accommodation.ReservationUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {

    // 예약을 등록하는 메서드
    void reservationRegister(Reservation reservation);

    // 예약 상태를 업데이트하는 메서드
    void updateReservationStatus(Reservation reservation);

    // 특정 ID를 가진 예약을 찾는 메서드
    Reservation findReservation(Long id);

    // 주어진 날짜 범위가 방의 예약 날짜와 겹치는지 확인하는 메서드
    boolean isDateOverlapping(Long roomId, LocalDate checkInDate, LocalDate checkOutDate);

    // 예약 UID를 사용하여 예약을 삭제하는 메서드
    void removeReservation(String reservationUid);

    // 특정 방의 예약 목록을 반환하는 메서드
    List<Reservation> getReservationsByRoomId(Long roomId);

    // 예약 정보를 업데이트하는 메서드
    Reservation updateReservation(ReservationUpdateDTO reservationDTO, String username);

    // 예약 날짜가 유효한지 검증하는 메서드
    void validateReservationDates(ReservationDTO reservationDTO, Room room);

    // 회원의 예약 목록을 페이지 단위로 반환하는 메서드
    Page<Reservation> getReservationsByPage(Member member, Pageable pageable);

    // 예약 UID를 사용하여 예약을 취소하는 메서드
    boolean cancelReservation(String reservationUid);

    Reservation createReservation(Member member, Room room, ReservationDTO reservationDTO);
}