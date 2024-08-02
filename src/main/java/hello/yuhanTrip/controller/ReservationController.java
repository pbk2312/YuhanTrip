package hello.yuhanTrip.controller;


import hello.yuhanTrip.domain.*;
import hello.yuhanTrip.dto.ReservationDTO;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.repository.PaymentRepository;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
import hello.yuhanTrip.service.Accomodation.ReservationService;
import hello.yuhanTrip.service.MemberService;
import hello.yuhanTrip.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Log4j2
public class ReservationController {

    private final TokenProvider tokenProvider;
    private final MemberService memberService;
    private final AccommodationService accommodationService;
    private final ReservationService reservationService;
    private final PaymentRepository paymentRepository;

    @GetMapping("/reservation")
    public String getReservation(
            @RequestParam("id") Long id,
            Model model,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            return "redirect:/member/login";
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.info("예약 시도 유저 : {}", userDetails.getUsername());

        Accommodation accommodationInfo = accommodationService.getAccommodationInfo(id);
        log.info("숙소 이름 : {} ", accommodationInfo.getTitle());


        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setId(accommodationInfo.getId());
        reservationDTO.setAccommodationTitle(accommodationInfo.getTitle());
        reservationDTO.setPrice(accommodationInfo.getPrice());
        reservationDTO.setLocalDate(LocalDate.now());

        model.addAttribute("reservation", reservationDTO);
        return "reservation";
    }


    @GetMapping("/reservation/fail")
    public String reservationFailAndGoHome(
            @RequestParam("reservationId") String reservationUId,
            Model model
    ) {
        try {
            reservationService.removeReservation(reservationUId);
        } catch (RuntimeException e) {
            // 예약이 없을 경우 사용자에게 오류 메시지를 보여줍니다.
            model.addAttribute("errorMessage", "예약 정보가 없습니다.");
            return "accommodations"; // 오류 페이지로 이동 (적절한 오류 페이지를 설정)
        } catch (Exception e) {
            // 기타 예외 처리
            model.addAttribute("errorMessage", "예약 삭제 중 오류가 발생했습니다.");
            return "accommodations"; // 오류 페이지로 이동
        }

        return "redirect:/home/homepage";
    }



    @PostMapping("/reservation/submit")
    public ResponseEntity<Map<String, Object>> submitReservation(
            @RequestBody ReservationDTO reservationDTO,
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        log.info("예약 요청을 처리합니다...");

        log.info("Received ReservationDTO: {}", reservationDTO);
        log.info("Accommodation ID from DTO: {}", reservationDTO.getAccommodationId());

        // 인증 확인
        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            log.info("사용자가 인증되지 않았습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "유저를 찾을 수 없습니다."));
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.info("예약 시도 유저: {}", userDetails.getUsername());

        try {
            // 숙소 정보 조회
            Accommodation accommodation = accommodationService.getAccommodationInfo(reservationDTO.getAccommodationId());
            if (accommodation == null) {
                log.warn("숙소를 찾을 수 없습니다: ID = {}", reservationDTO.getAccommodationId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "숙소를 찾을 수 없습니다."));
            }

            // 체크인 및 체크아웃 날짜 검증
            LocalDate today = LocalDate.now();
            if (reservationDTO.getCheckInDate().isBefore(today)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "체크인 날짜는 오늘보다 이전일 수 없습니다."));
            }

            if (reservationDTO.getCheckOutDate().isBefore(reservationDTO.getCheckInDate()) ||
                    reservationDTO.getCheckOutDate().isEqual(reservationDTO.getCheckInDate())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "체크아웃 날짜는 체크인 날짜보다 이후여야 합니다."));
            }

            // 기존 예약 확인
            boolean dateOverlapping = reservationService.isDateOverlapping(
                    reservationDTO.getAccommodationId(),
                    reservationDTO.getCheckInDate(),
                    reservationDTO.getCheckOutDate()
            );

            if (dateOverlapping) { // 선택한 날짜가 이미 예약이 존재하면
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "선택한 날짜에 이미 예약이 있습니다."));
            }

            // 총 가격 계산
            long numberOfNights = ChronoUnit.DAYS.between(reservationDTO.getCheckInDate(), reservationDTO.getCheckOutDate());
            Long totalPrice = accommodation.getPrice() * numberOfNights;

            // 결제 정보 저장
            Payment payment = new Payment(totalPrice, PaymentStatus.PENDING);
            Payment savedPayment = paymentRepository.save(payment); // Payment 저장

            // 사용자 정보 및 예약 저장
            Member member = memberService.findByEmail(userDetails.getUsername());
            Reservation reservation = Reservation.builder()
                    .addr(reservationDTO.getAddr())
                    .reservationUid(UUID.randomUUID().toString()) // 예약번호 생성 및 설정
                    .member(member)
                    .accommodation(accommodation)
                    .checkInDate(reservationDTO.getCheckInDate())
                    .checkOutDate(reservationDTO.getCheckOutDate())
                    .reservationDate(today)
                    .specialRequests(reservationDTO.getSpecialRequests())
                    .name(reservationDTO.getName())
                    .phoneNumber(reservationDTO.getPhoneNumber())
                    .payment(savedPayment) // 저장된 Payment 객체 설정
                    .numberOfGuests(reservationDTO.getNumberOfGuests())
                    .build();

            reservationService.reservationRegister(reservation);

            log.info("예약이 성공적으로 완료되었습니다: 예약 ID = {}", reservation.getId());

            // 성공적인 응답 반환
            Map<String, Object> response = new HashMap<>();
            response.put("message", "예약이 성공적으로 완료되었습니다.");
            response.put("reservationId", reservation.getId());
            response.put("totalPrice", totalPrice);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("예약 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "예약 처리 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/reservationConfirm")
    public String successPaymentPage(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            Model model
    ) {

        // 인증 확인
        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            return "redirect:/member/login"; // 로그인 페이지로 리다이렉트
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.info("예약 확정 확인 유저 : {}", userDetails.getUsername());

        Member member = memberService.findByEmail(userDetails.getUsername());

        List<Reservation> reservations = member.getReservations();

        // 모델에 예약 리스트 추가
        model.addAttribute("reservations", reservations);


        return "reservationConfirms";
    }

    @GetMapping("/fail-payment")
    public String failPaymentPage() {
        return "fail-payment";
    }
}
