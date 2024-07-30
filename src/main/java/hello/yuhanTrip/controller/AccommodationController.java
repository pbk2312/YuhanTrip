package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.domain.PaymentStatus;
import hello.yuhanTrip.domain.Reservation;
import hello.yuhanTrip.dto.ReservationDTO;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
import hello.yuhanTrip.service.Accomodation.ReservationService;
import hello.yuhanTrip.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
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


@Controller
@RequiredArgsConstructor
@RequestMapping("/accommodation")
@Log4j2
public class AccommodationController {

    private final AccommodationService accommodationService;
    private final ReservationService reservationService;
    private final TokenProvider tokenProvider;
    private final MemberService memberService;


    @GetMapping("/accommodations")
    public String listAccommodations(Model model,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size
    ) {

        log.info("숙소 리스트를 조회합니다.... 페이지: {}, 사이즈: {}", page, size);
        // 페이지 번호와 사이즈 검증
        page = Math.max(page, 0);
        size = Math.max(size, 1);


        Page<Accommodation> accommodationsPage = accommodationService.getAccommodations(page, size);

        int totalPages = accommodationsPage.getTotalPages();
        int currentPage = page;

        // 페이지 번호 범위 계산
        int startPage = Math.max(0, currentPage - 5);
        int endPage = Math.min(totalPages - 1, currentPage + 5);

        model.addAttribute("accommodations", accommodationsPage.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("pageSize", size);

        log.info("현재 페이지: {}, 전체 페이지: {}, 시작 페이지: {}, 끝 페이지: {}", currentPage, totalPages, startPage, endPage);

        return "accommodations";
    }


    @GetMapping("/info")
    public String getAccommodationInfo(@RequestParam("id") Long id, Model model) {


        log.info("숙소 정보를 가져옵니다... = {}", id);

        // 숙소 정보를 가져옵니다.
        Accommodation accommodation = accommodationService.getAccommodationInfo(id);

        // 숙소 정보가 존재하지 않는 경우 에러 페이지로 리다이렉트 또는 404 오류 페이지로 이동
        if (accommodation == null) {
            return "error/404"; // 또는 "redirect:/error/404"
        }

        // 모델에 숙소 정보를 추가하여 뷰로 전달합니다.
        model.addAttribute("accommodation", accommodation);

        // 상세 페이지로 이동합니다.
        return "accommodationInfo"; // 상세 페이지의 뷰 이름

    }


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
            int totalPrice = accommodation.getPrice() * (int) numberOfNights;

            // 사용자 정보 및 예약 저장
            Member member = memberService.findByEmail(userDetails.getUsername());
            Reservation reservation = Reservation.builder()
                    .member(member)
                    .accommodation(accommodation)
                    .checkInDate(reservationDTO.getCheckInDate())
                    .checkOutDate(reservationDTO.getCheckOutDate())
                    .reservationDate(today)
                    .specialRequests(reservationDTO.getSpecialRequests())
                    .name(reservationDTO.getName())
                    .phoneNumber(reservationDTO.getPhoneNumber())
                    .price(totalPrice)
                    .paymentStatus(PaymentStatus.PENDING) // 예약 생성 시 결제 대기 상태로 설정
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


}
