package hello.yuhanTrip.controller.restApi;

import hello.yuhanTrip.domain.accommodation.Room;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.domain.reservation.Payment;
import hello.yuhanTrip.domain.reservation.PaymentStatus;
import hello.yuhanTrip.domain.reservation.Reservation;
import hello.yuhanTrip.domain.reservation.ReservationStatus;
import hello.yuhanTrip.dto.accommodation.ReservationDTO;
import hello.yuhanTrip.dto.accommodation.ReservationUpdateDTO;
import hello.yuhanTrip.repository.PaymentRepository;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
import hello.yuhanTrip.service.Accomodation.ReservationService;
import hello.yuhanTrip.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@Log4j2
public class ReservationApiController {

    private final MemberService memberService;
    private final AccommodationService accommodationService;
    private final ReservationService reservationService;
    private final PaymentRepository paymentRepository;


    // 예약 신청
    @PostMapping("/reservation/submit")
    public ResponseEntity<Map<String, Object>> submitReservation(
            @RequestBody ReservationDTO reservationDTO,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        log.info("예약 요청을 처리합니다...");

        Member member = memberService.getUserDetails(accessToken);
        try {
            Room room = accommodationService.getRoomInfo(reservationDTO.getRoomId());
            if (room == null) {
                return createErrorResponse("숙소를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
            }

            reservationService.validateReservationDates(reservationDTO, room);
            boolean dateOverlapping = reservationService.isDateOverlapping(
                    reservationDTO.getRoomId(),
                    reservationDTO.getCheckInDate(),
                    reservationDTO.getCheckOutDate()
            );

            if (dateOverlapping) {
                return createErrorResponse("선택한 날짜에 이미 예약이 있습니다.", HttpStatus.CONFLICT);
            }

            long numberOfNights = ChronoUnit.DAYS.between(reservationDTO.getCheckInDate(), reservationDTO.getCheckOutDate());
            Long totalPrice = reservationDTO.getPrice() * numberOfNights;


            Payment payment = new Payment(totalPrice, PaymentStatus.PENDING);
            Payment savedPayment = paymentRepository.save(payment);

            Reservation reservation = Reservation.builder()
                    .addr(reservationDTO.getAddr())
                    .reservationUid(UUID.randomUUID().toString())
                    .member(member)
                    .room(room)
                    .checkInDate(reservationDTO.getCheckInDate())
                    .checkOutDate(reservationDTO.getCheckOutDate())
                    .reservationDate(LocalDate.now())
                    .specialRequests(reservationDTO.getSpecialRequests())
                    .name(reservationDTO.getName())
                    .phoneNumber(reservationDTO.getPhoneNumber())
                    .payment(savedPayment)
                    .numberOfGuests(reservationDTO.getNumberOfGuests())
                    .accommodationId(room.getAccommodation().getId())
                    .reservationStatus(ReservationStatus.RESERVED)
                    .couponId(reservationDTO.getCouponId())
                    .build();

            reservationService.reservationRegister(reservation);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "예약이 성공적으로 완료되었습니다.");
            response.put("reservationId", reservation.getId());
            response.put("totalPrice", totalPrice);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return createErrorResponse("예약 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/updateReservation")
    public ResponseEntity<Map<String, Object>> updateReservation(
            @RequestBody ReservationUpdateDTO reservationUpdateDTO,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        Member member = memberService.getUserDetails(accessToken);
        try {
            validateReservationUpdateDates(reservationUpdateDTO);

            Reservation reservation = reservationService.findReservation(reservationUpdateDTO.getId());
            boolean dateOverlapping = reservationService.isDateOverlapping(
                    reservation.getRoom().getId(),
                    reservationUpdateDTO.getCheckInDate(),
                    reservationUpdateDTO.getCheckOutDate()
            );

            if (dateOverlapping) {
                return createErrorResponse("선택한 날짜에 이미 예약이 있습니다.", HttpStatus.CONFLICT);
            }

            reservationService.updateReservation(reservationUpdateDTO, member.getName());

            return ResponseEntity.ok(Map.of("message", "예약이 성공적으로 수정되었습니다."));

        } catch (RuntimeException e) {
            return createErrorResponse("예약 수정 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(Map.of("message", message));
    }

    private void validateReservationUpdateDates(ReservationUpdateDTO dto) {
        LocalDate today = LocalDate.now();
        if (dto.getCheckInDate().isBefore(today)) {
            throw new IllegalArgumentException("체크인 날짜는 오늘보다 이전일 수 없습니다.");
        }
        if (dto.getCheckOutDate().isBefore(dto.getCheckInDate()) ||
                dto.getCheckOutDate().isEqual(dto.getCheckInDate())) {
            throw new IllegalArgumentException("체크아웃 날짜는 체크인 날짜보다 이후여야 합니다.");
        }
    }
}
