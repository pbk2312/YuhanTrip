package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.Reservation;
import hello.yuhanTrip.dto.PaymentDTO;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Log4j2
public class PaymentController {

    private final ReservationRepository reservationRepository;
    private final TokenProvider tokenProvider;

    @GetMapping("/paymentPage")
    public String paymentPage(
            @RequestParam("reservationId") Long reservationId,
            @RequestParam("totalPrice") Integer totalPrice,
            @CookieValue(value = "accessToken", required = false) String accessToken,
            Model model
    ) {

        // 인증 확인
        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            return "redirect:/member/login"; // 로그인 페이지로 리다이렉트
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.info("예약 시도 유저 : {}", userDetails.getUsername());

        // 예약 정보 조회
        Reservation reservationInfo = reservationRepository.findById(reservationId).orElse(null);

        if (reservationInfo == null) {
            log.error("예약 정보를 찾을 수 없습니다. reservationId: {}", reservationId);
            return "redirect:/error"; // 예약 정보가 없을 경우 오류 페이지로 리다이렉트
        }

        log.info("예약자 이메일 : {}", userDetails.getUsername());
        log.info("숙소 예약자 : {}", reservationInfo.getName());
        log.info("총 가격 : {}", totalPrice);

        // 예약 정보를 PaymentDTO에 담기
        PaymentDTO paymentDTO = PaymentDTO.builder()
                .reservationId(reservationInfo.getId())
                .memberId(reservationInfo.getMember().getId())
                .accommodationId(reservationInfo.getAccommodation().getId())
                .accommodationTitle(reservationInfo.getAccommodation().getTitle())
                .reservationDate(reservationInfo.getReservationDate())
                .checkInDate(reservationInfo.getCheckInDate())
                .checkOutDate(reservationInfo.getCheckOutDate())
                .name(reservationInfo.getName())
                .phoneNumber(reservationInfo.getPhoneNumber())
                .specialRequests(reservationInfo.getSpecialRequests())
                .totalPrice(totalPrice)
                .build();

        // 모델에 PaymentDTO 추가
        model.addAttribute("paymentInfo", paymentDTO);

        // 결제 페이지로 이동
        return "paymentPage";
    }


}
