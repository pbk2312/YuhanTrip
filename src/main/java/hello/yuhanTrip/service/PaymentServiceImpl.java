package hello.yuhanTrip.service;


import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import hello.yuhanTrip.domain.PaymentStatus;
import hello.yuhanTrip.domain.Reservation;
import hello.yuhanTrip.dto.payment.PaymentCallbackRequest;
import hello.yuhanTrip.dto.payment.PaymentDTO;
import hello.yuhanTrip.repository.PaymentRepository;
import hello.yuhanTrip.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{

    private final ReservationRepository reservationRepository;
    private PaymentCallbackRequest paymentCallbackRequest;
    private IamportClient iamportClient;
    private PaymentRepository paymentRepository;

    @Override
    public PaymentDTO findRequestDto(String reservationUid) {
        Reservation reservation = reservationRepository.findReservationAndPaymentAndMember(reservationUid)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않아요"));

        return PaymentDTO.builder()
                .buyerName(reservation.getName())
                .accommodationTitle(reservation.getAccommodation().getTitle())
                .specialRequests(reservation.getSpecialRequests())
                .reservationDate(reservation.getReservationDate())
                .checkInDate(reservation.getCheckInDate())
                .totalPrice(reservation.getPrice())
                .checkOutDate(reservation.getCheckOutDate())
                .accommodationId(reservation.getId())
                .phoneNumber(reservation.getPhoneNumber())
                .reservationUid(reservationUid)
                .build();
    }

    @Override
    public IamportResponse<Payment> paymentByCallback(PaymentCallbackRequest request) {
        try {
            // 결제 단건 조회(아임포트)
            IamportResponse<Payment> iamportResponse = iamportClient.paymentByImpUid(request.getPaymentUid());
            // 주문내역 조회
            Reservation reservation = reservationRepository.findReservationAndPayment(request.getReservationUid())
                    .orElseThrow(() -> new IllegalArgumentException("주문 내역이 없습니다."));

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

            return iamportResponse;

        } catch (IamportResponseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    }
