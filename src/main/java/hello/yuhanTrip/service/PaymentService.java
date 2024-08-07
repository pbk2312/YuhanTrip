package hello.yuhanTrip.service;

import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import hello.yuhanTrip.dto.payment.PaymentCallbackRequest;
import hello.yuhanTrip.dto.payment.PaymentDTO;

public interface PaymentService {

    // 결제 요청 데이터 조회
    PaymentDTO findRequestDto(String reservationUid);

    // 결제(콜백)
    IamportResponse<Payment> paymentByCallback(PaymentCallbackRequest request);

    hello.yuhanTrip.domain.Payment findPayment(String paymentUid);

}
