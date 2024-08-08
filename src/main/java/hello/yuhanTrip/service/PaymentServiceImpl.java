package hello.yuhanTrip.service;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import hello.yuhanTrip.domain.PaymentAccessToken;
import hello.yuhanTrip.domain.PaymentStatus;
import hello.yuhanTrip.domain.Reservation;
import hello.yuhanTrip.dto.payment.PaymentCallbackRequest;
import hello.yuhanTrip.dto.payment.PaymentCancelDTO;
import hello.yuhanTrip.dto.payment.PaymentDTO;
import hello.yuhanTrip.repository.PaymentAccessTokenRepository;
import hello.yuhanTrip.repository.PaymentRepository;
import hello.yuhanTrip.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class PaymentServiceImpl implements PaymentService {


    private final PaymentAccessTokenRepository paymentAccessTokenRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final IamportClient iamportClient;

    @Override
    public PaymentDTO findRequestDto(String reservationUid) {
        Reservation reservation = reservationRepository.findReservationAndPaymentAndMember(reservationUid)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않아요"));

        return PaymentDTO.builder()
                .buyerName(reservation.getName())
                .roomId(reservation.getRoom().getId())
                .roomNm(reservation.getRoom().getRoomNm())
                .accommodationTitle(reservation.getRoom().getAccommodation().getTitle())
                .specialRequests(reservation.getSpecialRequests())
                .reservationDate(reservation.getReservationDate())
                .checkInDate(reservation.getCheckInDate())
                .totalPrice(reservation.getPayment().getPrice())
                .checkOutDate(reservation.getCheckOutDate())
                .accommodationId(reservation.getId())
                .phoneNumber(reservation.getPhoneNumber())
                .reservationUid(reservationUid)
                .numberOfGuests(reservation.getNumberOfGuests())
                .build();
    }

    @Override
    public IamportResponse<Payment> paymentByCallback(PaymentCallbackRequest request) {
        try {
            log.info("결제 시도..");

            // 결제 단건 조회(아임포트)
            IamportResponse<Payment> iamportResponse = iamportClient.paymentByImpUid(request.getPaymentUid());
            // 주문내역 조회
            Reservation reservation = reservationRepository.findReservationAndPayment(request.getReservationUid())
                    .orElseThrow(() -> new IllegalArgumentException("예약 내역이 없습니다."));

            // 결제 완료가 아니면
            if (!iamportResponse.getResponse().getStatus().equals("paid")) {
                // 주문, 결제 삭제
                reservationRepository.delete(reservation);
                paymentRepository.delete(reservation.getPayment());

                throw new RuntimeException("결제 미완료");
            }

            // DB에 저장된 결제 금액
            Long price = reservation.getPayment().getPrice();
            // 실 결제 금액
            int iamportPrice = iamportResponse.getResponse().getAmount().intValue();


            // 결제 금액 검증
            if (iamportPrice != price) {
                // 주문, 결제 삭제
                reservationRepository.delete(reservation);
                paymentRepository.delete(reservation.getPayment());

                // 결제금액 위변조로 의심되는 결제금액을 취소(아임포트)
                iamportClient.cancelPaymentByImpUid(new CancelData(iamportResponse.getResponse().getImpUid(), true, new BigDecimal(iamportPrice)));

                throw new RuntimeException("결제금액 위변조 의심");
            }

            // 결제 상태 변경
            reservation.getPayment().changePaymentBySuccess(PaymentStatus.COMPLETED, iamportResponse.getResponse().getImpUid());


            log.info("결제 완료...");
            return iamportResponse;

        } catch (IamportResponseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public hello.yuhanTrip.domain.Payment findPayment(String paymentUid) {
        hello.yuhanTrip.domain.Payment byPaymentUid = paymentRepository.findByPaymentUid(paymentUid);
        return byPaymentUid;
    }

    @Override
    public void remove(String paymentUid) {
        hello.yuhanTrip.domain.Payment payment = findPayment(paymentUid);
        paymentRepository.delete(payment);
    }



    public void cancelReservation(PaymentCancelDTO paymentCancelDTO) {
        try {
            IamportResponse<Payment> response = iamportClient.paymentByImpUid(paymentCancelDTO.getPaymentUid());

            if (response == null || response.getResponse() == null) {
                throw new IllegalArgumentException("Invalid payment information.");
            }

            // refundAmount가 Long 타입인 경우, int로 변환
            int refundAmount = paymentCancelDTO.getCancelRequestAmount().intValue();

            CancelData cancelData = createCancelData(response, refundAmount);
            IamportResponse<Payment> cancelResponse = iamportClient.cancelPaymentByImpUid(cancelData);

            // 환불 처리 결과에 대한 추가 로직
            if (cancelResponse.getCode() != 0) {
                throw new RuntimeException("Failed to cancel payment: " + cancelResponse.getMessage());
            }

        } catch (IamportResponseException e) {
            // Iamport API 응답 예외 처리
            e.printStackTrace();
            // 적절한 오류 처리 로직
        } catch (IOException e) {
            // I/O 예외 처리
            e.printStackTrace();
            // 적절한 오류 처리 로직
        } catch (Exception e) {
            // 기타 예외 처리
            e.printStackTrace();
            // 적절한 오류 처리 로직
        }
    }

    private CancelData createCancelData(IamportResponse<Payment> response, int refundAmount) {
        if (refundAmount == 0) { //전액 환불일 경우
            return new CancelData(response.getResponse().getImpUid(), true);
        }
        //부분 환불일 경우 checksum을 입력해 준다.
        return new CancelData(response.getResponse().getImpUid(), true, new BigDecimal(refundAmount));

    }
}
