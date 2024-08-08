package hello.yuhanTrip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import hello.yuhanTrip.domain.PaymentAccessToken;
import hello.yuhanTrip.domain.Reservation;
import hello.yuhanTrip.dto.payment.PaymentCallbackRequest;
import hello.yuhanTrip.dto.payment.PaymentCancelDTO;
import hello.yuhanTrip.dto.payment.PaymentDTO;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.repository.PaymentAccessTokenRepository;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


import java.util.HashMap;
import java.util.Map;


@Controller
@RequiredArgsConstructor
@Log4j2
public class PaymentController {

    private final ReservationRepository reservationRepository;
    private final TokenProvider tokenProvider;
    private final PaymentService paymentService;
    private final PaymentAccessTokenRepository paymentAccessTokenRepository;
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
        String paymentUid = reservation.getPayment().getPaymentUid();
        hello.yuhanTrip.domain.Payment payment = paymentService.findPayment(paymentUid);

        PaymentCancelDTO paymentCancelDTO = new PaymentCancelDTO();
        paymentCancelDTO.setPaymentUid(paymentUid);
        paymentCancelDTO.setCancelRequestAmount(payment.getPrice());
        paymentCancelDTO.setReservationId(reservationId);

        String accessToken = getAccessToken();
        if (accessToken != null) {
            PaymentAccessToken paymentAccessToken = new PaymentAccessToken();
            paymentAccessToken.setReservationId(reservationId);
            paymentAccessToken.setPaymentAccessToken(accessToken);
            paymentAccessTokenRepository.save(paymentAccessToken);
            log.info("Access Token: {}", accessToken);
        } else {
            log.error("Failed to retrieve access token.");
        }

        model.addAttribute("paymentCancelDTO", paymentCancelDTO);
        return "reservationCancel";
    }



    @PostMapping("/payment/cancel")
    public ResponseEntity<String> cancelPayment(@RequestBody PaymentCancelDTO paymentCancelDTO) {
        try {
            // 결제 취소 처리
            paymentService.cancelReservation(paymentCancelDTO);

            // 성공적인 경우 200 OK 응답
            return ResponseEntity.ok("결제 취소 요청이 성공적으로 처리되었습니다.");

        } catch (Exception e) {
            // 예외 처리 및 500 Internal Server Error 응답
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("결제 취소 요청 처리 중 오류가 발생했습니다.");
        }
    }



    private String getAccessToken() {
        String url = "https://api.iamport.kr/users/getToken";
        RestTemplate restTemplate = new RestTemplate();

        log.info("Request URL: {}", url);
        log.info("imp_key: {}", impKey);
        log.info("imp_secret: {}", impSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("imp_key", impKey);
        body.add("imp_secret", impSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);


        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        log.info("Response Status Code: {}", response.getStatusCode());
        log.info("Response Body: {}", response.getBody());

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                Map<String, String> responseData = (Map<String, String>) responseBody.get("response");
                return responseData.get("access_token");
            }
        }

        return null;
    }
}
