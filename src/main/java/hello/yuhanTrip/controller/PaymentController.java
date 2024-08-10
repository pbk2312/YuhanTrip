package hello.yuhanTrip.controller;

import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import hello.yuhanTrip.domain.CancelReservation;
import hello.yuhanTrip.domain.Reservation;
import hello.yuhanTrip.dto.ReservationDTO;
import hello.yuhanTrip.dto.payment.PaymentCallbackRequest;
import hello.yuhanTrip.dto.payment.PaymentCancelDTO;
import hello.yuhanTrip.dto.payment.PaymentDTO;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.repository.ReservationRepository;
import hello.yuhanTrip.service.Accomodation.ReservationService;
import hello.yuhanTrip.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequiredArgsConstructor
@Log4j2
public class PaymentController {

    private final ReservationRepository reservationRepository;
    private final TokenProvider tokenProvider;
    private final PaymentService paymentService;
    private final ReservationService reservationService;

    @Value("${imp_key}")
    private String impKey;

    @Value("${imp_secret}")
    private String impSecret;

    @GetMapping("/paymentPage")
    public String paymentPage(
            @RequestParam("reservationId") Long reservationId,
            @RequestParam("totalPrice") Long totalPrice,
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
        String reservationUid = reservationInfo.getReservationUid();
        log.info("reservationUid: {}", reservationUid);
        PaymentDTO requestDto = paymentService.findRequestDto(reservationUid);
        requestDto.setAddr(userDetails.getUsername());


        log.info("paymentRequest = {} ", requestDto);
        log.info("예약자 이메일 : {}", userDetails.getUsername());
        log.info("숙소 예약자 : {}", reservationInfo.getName());
        log.info("총 가격 : {}", totalPrice);

        // 예약 정보를 PaymentDTO에 담기
        // 모델에 PaymentDTO 추가
        model.addAttribute("requestDto", requestDto);

        // 결제 페이지로 이동
        return "paymentPage";
    }


    @ResponseBody
    @PostMapping("/payment")
    public ResponseEntity<IamportResponse<Payment>> validationPayment(@RequestBody PaymentCallbackRequest request) {
        IamportResponse<Payment> iamportResponse = paymentService.paymentByCallback(request);

        log.info("결제 응답={}", iamportResponse.getResponse().toString());

        return new ResponseEntity<>(iamportResponse, HttpStatus.OK);
    }


    @GetMapping("/paymentCancelPage")
    public String paymentCancel(@RequestParam("reservationId") Long reservationId,
                                Model model) {

        log.info("환불 받을 예약 번호: {}", reservationId);


        Reservation reservation = reservationService.findReservation(reservationId);

        // ReservationDTO를 생성하고 필드를 설정함
        ReservationDTO reservationDTO = ReservationDTO.builder()
                .id(reservation.getId())
                .memberId(reservation.getMember().getId())
                .accommodationId(reservation.getAccommodationId())
                .roomId(reservation.getRoom().getId())
                .roomNm(reservation.getRoom().getRoomNm())  // Room 엔티티에 roomNm 필드가 있다고 가정
                .roomType(reservation.getRoom().getRoomType())  // Room 엔티티에 roomType 필드가 있다고 가정
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .reservationDate(reservation.getReservationDate())
                .specialRequests(reservation.getSpecialRequests())
                .accommodationTitle(reservation.getRoom().getAccommodation().getTitle())  // Accommodation 엔티티에 title 필드가 있다고 가정
                .name(reservation.getName())
                .phoneNumber(reservation.getPhoneNumber())
                .price(reservation.getPayment().getPrice())
                .addr(reservation.getAddr())
                .numberOfGuests(reservation.getNumberOfGuests())
                .build();


        String paymentUid = reservation.getPayment().getPaymentUid();
        hello.yuhanTrip.domain.Payment payment = paymentService.findPayment(paymentUid);

        PaymentCancelDTO paymentCancelDTO = new PaymentCancelDTO();
        paymentCancelDTO.setPaymentUid(paymentUid);
        paymentCancelDTO.setCancelRequestAmount(payment.getPrice());
        paymentCancelDTO.setReservationId(reservationId);


        model.addAttribute("reservationDTO", reservationDTO);
        model.addAttribute("paymentCancelDTO", paymentCancelDTO);
        return "/reservation/reservationCancel";
    }


    @PostMapping("/payment/cancel")
    public ResponseEntity<String> cancelPayment(@RequestBody PaymentCancelDTO paymentCancelDTO) {
        try {
            // 결제 취소 처리
            paymentService.cancelReservation(paymentCancelDTO);


            Long reservationId = paymentCancelDTO.getReservationId();
            Reservation reservation = reservationService.findReservation(reservationId);
            CancelReservation cancelReservation = CancelReservation.builder()
                    .reservationUid(reservation.getReservationUid())
                    .member(reservation.getMember())
                    .room(reservation.getRoom())
                    .accommodationId(reservation.getAccommodationId())
                    .checkInDate(reservation.getCheckInDate())
                    .checkOutDate(reservation.getCheckOutDate())
                    .reservationDate(reservation.getReservationDate())
                    .specialRequests(reservation.getSpecialRequests())
                    .name(reservation.getName())
                    .phoneNumber(reservation.getPhoneNumber())
                    .addr(reservation.getAddr())
                    .numberOfGuests(reservation.getNumberOfGuests())
                    .price(reservation.getPayment().getPrice())  // Reservation에 가격 필드가 있다고 가정
                    .build();


            reservationService.removeReservation(reservation.getReservationUid());
            reservationService.cancelReservationRegister(cancelReservation);
            paymentService.remove(paymentCancelDTO.getPaymentUid());
            // 성공적인 경우 200 OK 응답
            return ResponseEntity.ok("결제 취소 요청이 성공적으로 처리되었습니다.");

        } catch (Exception e) {
            // 예외 처리 및 500 Internal Server Error 응답
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("결제 취소 요청 처리 중 오류가 발생했습니다.");
        }
    }


}
