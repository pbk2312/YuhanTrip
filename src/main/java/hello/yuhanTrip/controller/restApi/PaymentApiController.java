package hello.yuhanTrip.controller.restApi;

import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import hello.yuhanTrip.domain.reservation.PaymentStatus;
import hello.yuhanTrip.domain.reservation.Reservation;
import hello.yuhanTrip.domain.reservation.ReservationStatus;
import hello.yuhanTrip.dto.payment.PaymentCallbackRequest;
import hello.yuhanTrip.dto.payment.PaymentCancelDTO;
import hello.yuhanTrip.service.reservation.ReservationService;
import hello.yuhanTrip.service.reservation.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@Log4j2
public class PaymentApiController {

    private final PaymentService paymentService;
    private final ReservationService reservationService;


    @PostMapping("/payment/cancel")
    public ResponseEntity<String> cancelPayment(@RequestBody PaymentCancelDTO paymentCancelDTO) {
        try {
            // 결제 취소 처리
            paymentService.cancelReservation(paymentCancelDTO);

            Long reservationId = paymentCancelDTO.getReservationId();
            Reservation reservation = reservationService.findReservation(reservationId);
            reservation.setReservationStatus(ReservationStatus.CANCELLED);
            reservationService.updateReservationStatus(reservation);


            hello.yuhanTrip.domain.reservation.Payment payment = paymentService.findPayment(paymentCancelDTO.getPaymentUid());



            paymentService.updatePaymentStatus(payment.getId(), PaymentStatus.CANCELLED);
            // 성공적인 경우 200 OK 응답
            return ResponseEntity.ok("결제 취소 요청이 성공적으로 처리되었습니다.");

        } catch (Exception e) {
            // 예외 처리 및 500 Internal Server Error 응답
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("결제 취소 요청 처리 중 오류가 발생했습니다.");
        }
    }

    // 결제 하기
    @ResponseBody
    @PostMapping("/payment")
    public ResponseEntity<IamportResponse<Payment>> validationPayment(@RequestBody PaymentCallbackRequest request) {
        IamportResponse<Payment> iamportResponse = paymentService.paymentByCallback(request);

        log.info("결제 응답={}", iamportResponse.getResponse().toString());

        return new ResponseEntity<>(iamportResponse, HttpStatus.OK);
    }


}