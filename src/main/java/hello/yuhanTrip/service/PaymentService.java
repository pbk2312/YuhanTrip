package hello.yuhanTrip.service;

import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import hello.yuhanTrip.domain.PaymentStatus;
import hello.yuhanTrip.dto.payment.PaymentCallbackRequest;
import hello.yuhanTrip.dto.payment.PaymentCancelDTO;
import hello.yuhanTrip.dto.payment.PaymentDTO;

public interface PaymentService {

    // 결제 요청 데이터 조회
    PaymentDTO findRequestDto(String reservationUid);

    // 결제(콜백)
    IamportResponse<Payment> paymentByCallback(PaymentCallbackRequest request);


    // 결제 조회
    hello.yuhanTrip.domain.Payment findPayment(String paymentUid);

    // 결제 삭제
    void remove(String paymentUid);


    // 예약 취소
    void cancelReservation(PaymentCancelDTO paymentCancelDTO);


    // 결제 상태 업데이트
    void updatePaymentStatus(Long paymentId, PaymentStatus status);



}
