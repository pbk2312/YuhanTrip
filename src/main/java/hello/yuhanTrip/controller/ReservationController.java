package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.*;
import hello.yuhanTrip.dto.ReservationDTO;
import hello.yuhanTrip.dto.ReservationUpdateDTO;
import hello.yuhanTrip.exception.UnauthorizedException;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.repository.PaymentRepository;
import hello.yuhanTrip.service.Accomodation.AccommodationServiceImpl;
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
    private final AccommodationServiceImpl accommodationService;
    private final ReservationService reservationService;
    private final PaymentRepository paymentRepository;


    // 예약하기 페이지
    @GetMapping("/reservation")
    public String getReservation(
            Model model,
            @RequestParam("id") Long roomId,
            @RequestParam(value = "checkin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkin,
            @RequestParam(value = "checkout", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkout,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        Member member = getUserDetails(accessToken);
        Room room = accommodationService.getRoomInfo(roomId);

        ReservationDTO reservationDTO = createReservationDTO(room, checkin, checkout, member);

        model.addAttribute("reservation", reservationDTO);
        model.addAttribute("room", room);
        return "/reservation/reservation";
    }


    // 예약실패
    @GetMapping("/reservation/fail")
    public String reservationFailAndGoHome(@RequestParam("reservationId") String reservationUId, Model model) {
        try {
            reservationService.removeReservation(reservationUId);
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "예약 정보가 없습니다.");
            return "/accommodation/accommodations";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "예약 삭제 중 오류가 발생했습니다.");
            return "/accommodation/accommodations";
        }

        return "redirect:/home/homepage";
    }


    // 예약 신청
    @PostMapping("/reservation/submit")
    public ResponseEntity<Map<String, Object>> submitReservation(
            @RequestBody ReservationDTO reservationDTO,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        log.info("예약 요청을 처리합니다...");

        Member member = getUserDetails(accessToken);

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


    // 예약 내역들
    @GetMapping("/reservationConfirm")
    public String successPaymentPage(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        Member member = getUserDetails(accessToken);
        log.info("예약 확정 확인 유저 : {}", member.getName());

        Pageable pageable = PageRequest.of(page, 4);
        Page<Reservation> reservationPage = reservationService.getReservationsByPage(member, pageable);
        LocalDate today = LocalDate.now();

        updateExpiredReservations(reservationPage.getContent(), today);

        List<Reservation> sortedReservations = sortReservations(reservationPage.getContent());

        model.addAttribute("reservations", sortedReservations);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reservationPage.getTotalPages());

        return "/reservation/reservationConfirms";
    }


    // 예약 업데이트
    @GetMapping("/reservationUpdate")
    public String reservationUpdate(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam("reservationId") Long id,
            Model model) {

        Member member = getUserDetails(accessToken);

        Reservation reservation = reservationService.findReservation(id);
        if (!reservation.getMember().getEmail().equals(member.getEmail())) {
            return "redirect:/accessDenied";
        }

        model.addAttribute("reservationInfo", reservation);

        return "/reservation/reservationUpdate";
    }

    @PostMapping("/updateReservation")
    public ResponseEntity<Map<String, Object>> updateReservation(
            @RequestBody ReservationUpdateDTO reservationUpdateDTO,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        Member member = getUserDetails(accessToken);

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


    // 예약 취소
    @GetMapping("/reservationConfirm/cancel")
    public String cancelPaymentPage(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @CookieValue(value = "accessToken", required = false) String accessToken,
            Model model) {

        Member member = getUserDetails(accessToken);

        List<Reservation> cancelledReservations = member.getReservations().stream()
                .filter(reservation -> ReservationStatus.CANCELLED.equals(reservation.getReservationStatus()))
                .collect(Collectors.toList());

        int pageSize = 4;
        int totalReservations = cancelledReservations.size();
        int totalPages = (int) Math.ceil((double) totalReservations / pageSize);
        page = Math.max(0, Math.min(page, totalPages - 1));

        List<Reservation> pagedReservations = cancelledReservations.stream()
                .skip(page * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());

        model.addAttribute("cancelReservations", pagedReservations);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "/reservation/reservationCancelConfirm";
    }


    // 결제 실패
    @GetMapping("/fail-payment")
    public String failPaymentPage() {
        return "fail-payment";
    }

    private Member getUserDetails(String accessToken) {
        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            throw new UnauthorizedException("인증되지 않은 사용자입니다.");
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.info("예약 시도 유저 : {}", userDetails.getUsername());

        return memberService.findByEmail(userDetails.getUsername());
    }

    private ReservationDTO createReservationDTO(Room room, LocalDate checkin, LocalDate checkout, Member member) {
        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setAccommodationId(room.getAccommodation().getId());
        reservationDTO.setAccommodationTitle(room.getAccommodation().getTitle());
        reservationDTO.setRoomId(room.getId());
        reservationDTO.setRoomNm(room.getRoomNm());
        reservationDTO.setRoomType(room.getRoomType());
        reservationDTO.setPrice(room.getPriceAsLong());
        reservationDTO.setLocalDate(LocalDate.now());
        reservationDTO.setCheckInDate(checkin);
        reservationDTO.setCheckOutDate(checkout);
        reservationDTO.setName(member.getName());
        reservationDTO.setAddr(member.getAddress());
        reservationDTO.setPhoneNumber(member.getPhoneNumber());
        return reservationDTO;
    }

    private void updateExpiredReservations(List<Reservation> reservations, LocalDate today) {
        reservations.stream()
                .filter(reservation -> reservation.getReservationStatus() == ReservationStatus.RESERVED &&
                        reservation.getCheckOutDate().isBefore(today))
                .forEach(reservation -> {
                    reservation.setReservationStatus(ReservationStatus.COMPLETED);
                    reservationService.updateReservationStatus(reservation);
                });
    }

    private List<Reservation> sortReservations(List<Reservation> reservations) {
        return reservations.stream()
                .sorted(Comparator.comparing(Reservation::getReservationStatus)
                        .thenComparing(Reservation::getCheckInDate))
                .collect(Collectors.toList());
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

    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(Map.of("message", message));
    }
}