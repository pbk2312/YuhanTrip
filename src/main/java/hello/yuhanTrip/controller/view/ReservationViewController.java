package hello.yuhanTrip.controller.view;

import hello.yuhanTrip.domain.accommodation.Room;
import hello.yuhanTrip.dto.coupon.Coupon;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.domain.reservation.Reservation;
import hello.yuhanTrip.domain.reservation.ReservationStatus;
import hello.yuhanTrip.dto.accommodation.ReservationDTO;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
import hello.yuhanTrip.service.RedisService;
import hello.yuhanTrip.service.reservation.ReservationService;
import hello.yuhanTrip.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Log4j2
public class ReservationViewController {

    private final MemberService memberService;
    private final AccommodationService accommodationService;
    private final ReservationService reservationService;
    private final RedisService redisService;


    // 예약하기 페이지
    @GetMapping("/reservation")
    public String getReservation(
            Model model,
            @RequestParam("id") Long roomId,
            @RequestParam(value = "checkin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkin,
            @RequestParam(value = "checkout", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkout,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        Member member = memberService.getUserDetails(accessToken);
        Room room = accommodationService.getRoomInfo(roomId);

        // 예약 정보를 생성
        ReservationDTO reservationDTO = createReservationDTO(room, checkin, checkout, member);

        // Redis에서 멤버의 쿠폰을 가져옴
        List<Coupon> coupons = redisService.getCouponsFromRedis(member);

        // 모델에 데이터를 추가
        model.addAttribute("reservation", reservationDTO);
        model.addAttribute("coupons", coupons); // 가져온 쿠폰들을 모델에 추가
        model.addAttribute("room", room);

        return "reservation/reservation";
    }

    // 예약실패
    @GetMapping("/reservation/fail")
    public String reservationFailAndGoHome(@RequestParam("reservationId") String reservationUId, Model model) {
        try {
            reservationService.removeReservation(reservationUId);
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "예약 정보가 없습니다.");
            return "accommodation/accommodations";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "예약 삭제 중 오류가 발생했습니다.");
            return "accommodation/accommodations";
        }

        return "redirect:/home/homepage";
    }



    // 예약 내역들
    @GetMapping("/reservationConfirm")
    public String successPaymentPage(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        Member member = memberService.getUserDetails(accessToken);
        log.info("예약 확정 확인 유저 : {}", member.getName());

        Pageable pageable = PageRequest.of(page, 4);
        Page<Reservation> reservationPage = reservationService.getReservationsByPage(member, pageable);
        LocalDate today = LocalDate.now();

        updateExpiredReservations(reservationPage.getContent(), today);

        List<Reservation> sortedReservations = sortReservations(reservationPage.getContent());

        model.addAttribute("reservations", sortedReservations);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reservationPage.getTotalPages());

        return "reservation/reservationConfirms";
    }


    // 예약 업데이트
    @GetMapping("/reservationUpdate")
    public String reservationUpdate(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam("reservationId") Long id,
            Model model) {

        Member member = memberService.getUserDetails(accessToken);
        Reservation reservation = reservationService.findReservation(id);
        if (!reservation.getMember().getEmail().equals(member.getEmail())) {
            return "redirect:/accessDenied";
        }

        model.addAttribute("reservationInfo", reservation);

        return "reservation/reservationUpdate";
    }




    // 예약 취소
    @GetMapping("/reservationConfirm/cancel")
    public String cancelPaymentPage(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @CookieValue(value = "accessToken", required = false) String accessToken,
            Model model) {

        Member member = memberService.getUserDetails(accessToken);
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

        return "reservation/reservationCancelConfirm";
    }



    // 결제 실패
    @GetMapping("/fail-payment")
    public String failPaymentPage() {
        return "error";
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




}