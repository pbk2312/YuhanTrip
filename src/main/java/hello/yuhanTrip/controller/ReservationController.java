package hello.yuhanTrip.controller;


import hello.yuhanTrip.domain.*;
import hello.yuhanTrip.dto.ReservationDTO;
import hello.yuhanTrip.dto.ReservationUpdateDTO;
import hello.yuhanTrip.exception.UnauthorizedException;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.repository.PaymentRepository;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
import hello.yuhanTrip.service.Accomodation.ReservationService;
import hello.yuhanTrip.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.*;
import java.util.stream.Collectors;

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


        Member member = getUserDetails(accessToken);

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
        reservationDTO.setName(member.getName());
        reservationDTO.setAddr(member.getAddress());
        reservationDTO.setPhoneNumber(member.getPhoneNumber());


        model.addAttribute("reservation", reservationDTO);
        return "/reservation/reservation";
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
            return "/accommodation/accommodations"; // 오류 페이지로 이동 (적절한 오류 페이지를 설정)
        } catch (Exception e) {
            // 기타 예외 처리
            model.addAttribute("errorMessage", "예약 삭제 중 오류가 발생했습니다.");
            return "/accommodation/accommodations"; // 오류 페이지로 이동
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

        Member member = getUserDetails(accessToken);

        try {
            // 객실 정보 조회
            Room room = accommodationService.getRoomInfo(reservationDTO.getRoomId());
            if (room == null) {
                log.warn("객실을 찾을 수 없습니다: ID = {}", reservationDTO.getRoomId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "숙소를 찾을 수 없습니다."));
            }

            // 체크인 및 체크아웃 날짜 검증 (서비스에서 처리)
            reservationService.validateReservationDates(reservationDTO, room);

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
            Reservation reservation = Reservation.builder()
                    .addr(reservationDTO.getAddr())
                    .reservationUid(UUID.randomUUID().toString()) // 예약번호 생성 및 설정
                    .member(member)
                    .room(room)
                    .checkInDate(reservationDTO.getCheckInDate())
                    .checkOutDate(reservationDTO.getCheckOutDate())
                    .reservationDate(LocalDate.now())
                    .specialRequests(reservationDTO.getSpecialRequests())
                    .name(reservationDTO.getName())
                    .phoneNumber(reservationDTO.getPhoneNumber())
                    .payment(savedPayment) // 저장된 Payment 객체 설정
                    .numberOfGuests(reservationDTO.getNumberOfGuests())
                    .accommodationId(room.getAccommodation().getId())
                    .reservationStatus(ReservationStatus.RESERVED)
                    .build();

            reservationService.reservationRegister(reservation);

            log.info("예약이 성공적으로 완료되었습니다: 예약 ID = {}", reservation.getId());

            // 성공적인 응답 반환
            Map<String, Object> response = new HashMap<>();
            response.put("message", "예약이 성공적으로 완료되었습니다.");
            response.put("reservationId", reservation.getId());
            response.put("totalPrice", totalPrice);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("예약 처리 중 검증 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("예약 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "예약 처리 중 오류가 발생했습니다."));
        }
    }




    @GetMapping("/reservationConfirm")
    public String successPaymentPage(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam(value = "page", defaultValue = "0") int page, // 현재 페이지 번호 (기본값: 0)
            Model model
    ) {
        // 1. 사용자 정보 조회
        Member member = getUserDetails(accessToken);
        log.info("예약 확정 확인 유저 : {}", member.getName());

        // 2. 페이지 처리 설정
        Pageable pageable = PageRequest.of(page, 4); // 한 페이지에 4개 예약

        // 3. 사용자의 예약 목록 조회 (페이지 처리)
        Page<Reservation> reservationPage = reservationService.getReservationsByPage(member, pageable);
        LocalDate today = LocalDate.now();

        // 4. 예약 상태를 확인하고 업데이트
        reservationPage.getContent().forEach(reservation -> {
            if (reservation.getReservationStatus() == ReservationStatus.RESERVED &&
                    reservation.getCheckOutDate().isBefore(today)) {
                reservation.setReservationStatus(ReservationStatus.COMPLETED);
                reservationService.updateReservationStatus(reservation); // 상태 업데이트 후 저장
            }
        });

        // 5. RESERVED 상태와 COMPLETED 상태의 예약을 나누어 필터링
        List<Reservation> reservedReservations = reservationPage.getContent().stream()
                .filter(reservation -> reservation.getReservationStatus() == ReservationStatus.RESERVED)
                .collect(Collectors.toList());

        List<Reservation> completedReservations = reservationPage.getContent().stream()
                .filter(reservation -> reservation.getReservationStatus() == ReservationStatus.COMPLETED)
                .collect(Collectors.toList());

        // 6. 두 리스트를 합쳐서 RESERVED 상태가 먼저 나오게 설정
        List<Reservation> sortedReservations = new ArrayList<>();
        sortedReservations.addAll(reservedReservations);
        sortedReservations.addAll(completedReservations);

        // 7. 모델에 정렬된 예약 리스트 추가
        model.addAttribute("reservations", sortedReservations);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reservationPage.getTotalPages());

        return "/reservation/reservationConfirms";
    }


    @GetMapping("/reservationUpdate")
    public String reservationUpdate(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam("reservationId") Long id,
            Model model
    ) {

        Member member = getUserDetails(accessToken);

        // 예약 정보 조회
        Reservation reservation = reservationService.findReservation(id);

        // 예약의 소유자와 로그인한 유저 비교
        if (!reservation.getMember().getEmail().equals(member.getEmail())) {
            log.info("예약자 이메일 : {} ,사용자 이메일 :{}", reservation.getMember().getEmail(), member.getEmail());
            // 유저가 예약의 소유자가 아니면 접근 거부 처리
            return "redirect:/accessDenied"; // 접근 거부 페이지로 리다이렉트
        }

        // 예약 정보 모델에 추가
        model.addAttribute("reservationInfo", reservation);

        return "/reservation/reservationUpdate";
    }


    @PostMapping("/updateReservation")
    public ResponseEntity<Map<String, Object>> updateReservation(
            @RequestBody ReservationUpdateDTO reservationUpdateDTO,
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {

        Member member = getUserDetails(accessToken);


        log.info("예약 수정 : {}", reservationUpdateDTO);
        Reservation reservation = reservationService.findReservation(reservationUpdateDTO.getId());


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
            reservationService.updateReservation(reservationUpdateDTO, member.getName());

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
            @RequestParam(value = "page", defaultValue = "0") int page,
            @CookieValue(value = "accessToken", required = false) String accessToken,
            Model model
    ) {
        // 1. 사용자 정보 조회
        Member member = getUserDetails(accessToken);

        // 2. 사용자의 예약 목록 조회
        List<Reservation> reservations = member.getReservations();

        // 3. CANCELLED 상태의 예약만 필터링
        List<Reservation> cancelledReservations = reservations.stream()
                .filter(reservation -> ReservationStatus.CANCELLED.equals(reservation.getReservationStatus()))
                .collect(Collectors.toList());

        // 4. 페이지 처리를 위한 페이징 설정
        int pageSize = 4; // 한 페이지에 표시할 예약 수
        int totalReservations = cancelledReservations.size();
        int totalPages = (int) Math.ceil((double) totalReservations / pageSize);

        // 페이지 번호 유효성 검사
        if (page < 0 || page >= totalPages) {
            page = 0;
        }

        // 페이지에 맞는 예약 리스트 추출
        int start = page * pageSize;
        int end = Math.min(start + pageSize, totalReservations);
        List<Reservation> pagedReservations = cancelledReservations.subList(start, end);

        // 5. 모델에 페이지 정보 및 취소된 예약 리스트 추가
        model.addAttribute("cancelReservations", pagedReservations);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "/reservation/reservationCancelConfirm";
    }



    @GetMapping("/fail-payment")
    public String failPaymentPage() {
        return "fail-payment";
    }


    public Member getUserDetails(String accessToken) {
        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            throw new UnauthorizedException("인증되지 않은 사용자입니다.");
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.info("예약 시도 유저 : {}", userDetails.getUsername());

        return memberService.findByEmail(userDetails.getUsername());
    }

}
