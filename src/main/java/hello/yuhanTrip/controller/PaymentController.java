package hello.yuhanTrip.controller;

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
                                Model model
    ) {


        log.info("환불 받을 려는 예약 번호 :{}", reservationId);

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
    public ResponseEntity<String> cancelPayment(@RequestBody PaymentCancelDTO cancelDTO) {
        log.info("결제 취소 요청: {}", cancelDTO);

        try {
            String paymentUid = cancelDTO.getPaymentUid();
            Long cancelRequestAmount = cancelDTO.getCancelRequestAmount();
            String reason = cancelDTO.getReason();
            Long reservationId = cancelDTO.getReservationId();

            hello.yuhanTrip.domain.Payment payment = paymentService.findPayment(paymentUid);

            // 결제정보로부터 imp_uid와 환불 가능 금액 계산
            String impUid = payment.getPaymentUid();
            Long cancelAmount = payment.getPrice();
            Long cancelableAmount = cancelAmount;

            if (cancelableAmount <= 0) {
                return ResponseEntity.badRequest().body("이미 전액 환불된 주문입니다.");
            }

            // 포트원 REST API로 결제 환불 요청

            PaymentAccessToken paymentAccessToken = paymentAccessTokenRepository.findByReservationId(reservationId).orElseThrow(() -> new RuntimeException("찾을 수 없다"));

            String accessToken = paymentAccessToken.getPaymentAccessToken();

            ResponseEntity<Map> getCancelDataResponse = cancelPaymentViaPortOne(impUid, reason, cancelRequestAmount, cancelableAmount, accessToken);

            if (getCancelDataResponse.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = getCancelDataResponse.getBody();
                if (responseBody != null) {
                    Map<String, Object> responseData = (Map<String, Object>) responseBody.get("response");
                    // 환불 결과 동기화 등 필요한 로직 추가
                    return ResponseEntity.ok("환불이 성공적으로 처리되었습니다.");
                }
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("환불 처리 중 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("환불 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("환불 처리 중 오류가 발생했습니다.");
        }
    }

    private ResponseEntity<Map> cancelPaymentViaPortOne(String impUid, String reason, Long cancelRequestAmount, Long cancelableAmount, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken); // 액세스 토큰 설정

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("reason", reason);
        requestBody.put("imp_uid", impUid);
        requestBody.put("amount", cancelRequestAmount);
        requestBody.put("checksum", cancelableAmount);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        return restTemplate.exchange("https://api.iamport.kr/payments/cancel", HttpMethod.POST, request, Map.class);
    }

    private String getAccessToken() {
        String url = "https://api.iamport.kr/users/getToken";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("imp_key", impKey);
        body.put("imp_secret", impSecret);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

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