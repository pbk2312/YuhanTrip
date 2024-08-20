package hello.yuhanTrip.service.Accomodation;

import hello.yuhanTrip.domain.*;
import hello.yuhanTrip.dto.ReservationDTO;
import hello.yuhanTrip.dto.ReservationUpdateDTO;
import hello.yuhanTrip.dto.payment.PaymentDTO;
import hello.yuhanTrip.repository.PaymentRepository;
import hello.yuhanTrip.repository.ReservationRepository;
import hello.yuhanTrip.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    @Transactional
    public void reservationRegister(Reservation reservation) {
        log.info("객실 예약 저장");
        Reservation reservationSet = reservationRepository.save(reservation);

        log.info("객실 예약 성공 : {}", reservationSet.getRoom().getAccommodation());


    }

    @Transactional
    public void updateReservationStatus(Reservation reservation) {
        reservationRepository.updateReservationStatus(reservation.getId(), reservation.getReservationStatus());
    }

    @Transactional
    public Reservation findReservation(Long id) {
        log.info("예약 정보 찾기");
        return reservationRepository.findById(id).orElseThrow(() -> new RuntimeException("예약 정보를 찾을 수 없다"));

    }

    public boolean isDateOverlapping(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        List<ReservationStatus> excludedStatuses = Arrays.asList(
                ReservationStatus.CANCELLED,
                ReservationStatus.REJECTED
        );

        List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(
                roomId,
                checkInDate,
                checkOutDate,
                excludedStatuses
        );

        boolean isOverlapping = !overlappingReservations.isEmpty();

        if (isOverlapping) {
            log.info("객실 ID {}에 대해 겹치는 예약이 발견되었습니다: {}", roomId, overlappingReservations);
        } else {
            log.info("객실 ID {}에 대해 겹치는 예약이 없습니다.", roomId);
        }

        return isOverlapping;
    }


    @Transactional
    public void removeReservation(String reservationUid) {
        Reservation reservation = reservationRepository.findByReservationUid(reservationUid)
                .orElseThrow(() -> new RuntimeException("예약 정보가 없다"));

        reservationRepository.delete(reservation);
    }

    public List<Reservation> getReservationsByRoomId(Long roomId) {
        return reservationRepository.findByRoomId(roomId);
    }


    public Reservation updateReservation(ReservationUpdateDTO reservationDTO, String username) {

        log.info("예약 수정...");
        // 예약 ID로 예약 조회
        Long id = reservationDTO.getId();
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        // 예약의 소유자와 요청한 유저의 사용자명 비교
        if (!reservation.getMember().getEmail().equals(username)) {
            throw new RuntimeException("권한이 없는 유저가 예약을 수정하려고 했습니다.");
        }

        // DTO를 통해 받은 데이터로 예약 업데이트
        reservation.setCheckInDate(reservationDTO.getCheckInDate());
        reservation.setCheckOutDate(reservationDTO.getCheckOutDate());
        reservation.setSpecialRequests(reservationDTO.getSpecialRequests());
        reservation.setNumberOfGuests(reservationDTO.getNumberOfGuests());

        // 변경된 예약 저장
        return reservationRepository.save(reservation);
    }

    public void validateReservationDates(ReservationDTO reservationDTO, Room room) {
        LocalDate today = LocalDate.now();

        // 체크인 날짜가 오늘 이전인지 확인
        if (reservationDTO.getCheckInDate().isBefore(today)) {
            throw new IllegalArgumentException("체크인 날짜는 오늘보다 이전일 수 없습니다.");
        }

        // 체크아웃 날짜가 체크인 날짜 이전 또는 동일한지 확인
        if (reservationDTO.getCheckOutDate().isBefore(reservationDTO.getCheckInDate()) ||
                reservationDTO.getCheckOutDate().isEqual(reservationDTO.getCheckInDate())) {
            throw new IllegalArgumentException("체크아웃 날짜는 체크인 날짜보다 이후여야 합니다.");
        }

        // 예약 인원이 객실 최대 수용 인원보다 많은지 확인
        if (reservationDTO.getNumberOfGuests() > room.getMaxOccupancy()) {
            throw new IllegalArgumentException("숙박 인원 수가 방 최대 수용 인원수 보다 많습니다");
        }
    }

    public Page<Reservation> getReservationsByPage(Member member, Pageable pageable) {
        return reservationRepository.findByMember(member, ReservationStatus.CANCELLED, pageable);
    }

    @Transactional
    public boolean cancelReservation(String reservationUid) {
        try {
            // 예약 정보 조회
            Reservation reservation = reservationRepository.findByReservationUid(reservationUid)
                    .orElseThrow(() -> new RuntimeException("예약 정보가 없다"));

            // 결제 정보 조회
            Payment payment = paymentService.findPayment(reservation.getPayment().getPaymentUid());
            if (payment == null) {
                throw new RuntimeException("결제 정보가 없다");
            }

            // 상태 변경
            reservation.setReservationStatus(ReservationStatus.REJECTED);
            payment.setStatus(PaymentStatus.CANCELLED);

            // 변경 사항 저장
            reservationRepository.save(reservation);
            paymentRepository.save(payment);

            return true;  // 예약이 성공적으로 취소되었음을 나타냄
        } catch (Exception e) {
            // 예외 발생 시 로그를 남기고 false 반환
            log.error("예약 취소 중 오류 발생: ", e);
            return false;
        }
    }
}
