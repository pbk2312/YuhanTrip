package hello.yuhanTrip.controller;


import hello.yuhanTrip.domain.*;
import hello.yuhanTrip.dto.ReservationDTO;
import hello.yuhanTrip.dto.ReservationUpdateDTO;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.repository.PaymentRepository;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
import hello.yuhanTrip.service.Accomodation.ReservationService;
import hello.yuhanTrip.service.MemberService;
import hello.yuhanTrip.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;
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
            Model model,
            @RequestParam("id") Long roomId,
            @RequestParam(value = "checkin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkin,
            @RequestParam(value = "checkout", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkout,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            return "redirect:/member/login";
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.info("예약 시도 유저 : {}", userDetails.getUsername());


        Room room = accommodationService.getRoomInfo(roomId);
        log.info("객실 이름 : {} ", room.getRoomType());
        log.info("숙소 이름 :{}", room.getAccommodation().getTitle());


        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setAccommodationId(room.getAccommodation().getId());
        reservationDTO.setAccommodationTitle(room.getAccommodation().getTitle());
        reservationDTO.setRoomId(roomId);
        reservationDTO.setRoomNm(room.getRoomNm());
        reservationDTO.setRoomType(room.getRoomType());
        reservationDTO.setPrice(room.getPriceAsLong());
        reservationDTO.setLocalDate(LocalDate.now());
        reservationDTO.setCheckInDate(checkin);
        reservationDTO.setCheckOutDate(checkout);


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
        log.info("Room ID from DTO: {}", reservationDTO.getRoomId());

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
            // 객실 정보 조회
            Room room = accommodationService.getRoomInfo(reservationDTO.getRoomId());
            if (room == null) {
                log.warn("객실을 찾을 수 없습니다: ID = {}", reservationDTO.getRoomId());
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

            if (reservationDTO.getNumberOfGuests() > room.getMaxOccupancy()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "숙박 인원 수가 방 최대 수용 인원수 보다 많습니다"));
            }


            // 기존 예약 확인
            boolean dateOverlapping = reservationService.isDateOverlapping(
                    reservationDTO.getRoomId(),
                    reservationDTO.getCheckInDate(),
                    reservationDTO.getCheckOutDate()
            );

            if (dateOverlapping) { // 선택한 날짜가 이미 예약이 존재하면
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "선택한 날짜에 이미 예약이 있습니다."));
            }

            // 총 가격 계산
            long numberOfNights = ChronoUnit.DAYS.between(reservationDTO.getCheckInDate(), reservationDTO.getCheckOutDate());
            Long totalPrice = reservationDTO.getPrice() * numberOfNights;

            // 결제 정보 저장
            Payment payment = new Payment(totalPrice, PaymentStatus.PENDING);
            Payment savedPayment = paymentRepository.save(payment); // Payment 저장

            // 사용자 정보 및 예약 저장
            Member member = memberService.findByEmail(userDetails.getUsername());
            Reservation reservation = Reservation.builder()
                    .addr(reservationDTO.getAddr())
                    .reservationUid(UUID.randomUUID().toString()) // 예약번호 생성 및 설정
                    .member(member)
                    .room(room)
                    .checkInDate(reservationDTO.getCheckInDate())
                    .checkOutDate(reservationDTO.getCheckOutDate())
                    .reservationDate(today)
                    .specialRequests(reservationDTO.getSpecialRequests())
                    .name(reservationDTO.getName())
                    .phoneNumber(reservationDTO.getPhoneNumber())
                    .payment(savedPayment) // 저장된 Payment 객체 설정
                    .numberOfGuests(reservationDTO.getNumberOfGuests())
                    .accommodationId(room.getAccommodation().getId())
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


    @GetMapping("/reservationUpdate")
    public String reservationUpdate(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam("reservationId") Long id,
            Model model
    ) {
        // 인증 확인
        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            return "redirect:/member/login"; // 로그인 페이지로 리다이렉트
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.info("예약 수정 유저 : {}", userDetails.getUsername());
        log.info("예약 수정 번호 : {}", id);

        // 예약 정보 조회
        Reservation reservation = reservationService.findReservation(id);

        // 예약의 소유자와 로그인한 유저 비교
        if (!reservation.getMember().getEmail().equals(userDetails.getUsername())) {
            // 유저가 예약의 소유자가 아니면 접근 거부 처리
            return "redirect:/accessDenied"; // 접근 거부 페이지로 리다이렉트
        }

        // 예약 정보 모델에 추가
        model.addAttribute("reservationInfo", reservation);

        return "reservationUpdate";
    }


    @PostMapping("/updateReservation")
    public ResponseEntity<Map<String, Object>> updateReservation(
            @RequestBody ReservationUpdateDTO reservationUpdateDTO,
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        log.info("예약 수정 : {}", reservationUpdateDTO);
        Reservation reservation = reservationService.findReservation(reservationUpdateDTO.getId());
        // 인증 확인
        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        try {
            // 체크인 및 체크아웃 날짜 검증
            LocalDate today = LocalDate.now();
            if (reservationUpdateDTO.getCheckInDate().isBefore(today)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "체크인 날짜는 오늘보다 이전일 수 없습니다."));
            }

            if (reservationUpdateDTO.getCheckOutDate().isBefore(reservationUpdateDTO.getCheckInDate()) ||
                    reservationUpdateDTO.getCheckOutDate().isEqual(reservationUpdateDTO.getCheckInDate())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "체크아웃 날짜는 체크인 날짜보다 이후여야 합니다."));
            }

            // 기존 예약 확인
            boolean dateOverlapping = reservationService.isDateOverlapping(
                    reservation.getRoom().getId(),
                    reservationUpdateDTO.getCheckInDate(),
                    reservationUpdateDTO.getCheckOutDate()
            );

            if (dateOverlapping) { // 선택한 날짜가 이미 예약이 존재하면
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "선택한 날짜에 이미 예약이 있습니다."));
            }

            log.info("예약 수정 시도...");
            reservationService.updateReservation(reservationUpdateDTO, userDetails.getUsername());

            // 성공 응답
            return ResponseEntity.ok(Map.of("message", "예약이 성공적으로 수정되었습니다."));

        } catch (RuntimeException e) {
            log.error("예약 수정 중 오류 발생: {}", e.getMessage());

            // 오류 응답
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "예약 수정 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/reservationConfirm/cancel")
    public String cancelPaymentPage(
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


        List<CancelReservation> cancelReservations = member.getCancelReservations();

        // 모델에 예약 리스트 추가
        model.addAttribute("cancelReservations", cancelReservations);


        return "reservationCancelConfirm";
    }







    @GetMapping("/fail-payment")
    public String failPaymentPage() {
        return "fail-payment";
    }



}
