package hello.yuhanTrip.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCancelDTO {

    /**
     * 결제 건의 주문 번호 (paymentUid UID)
     */
    private Long reservationId;
    private String paymentUid;

    /**
     * 환불 금액
     */
    private Long cancelRequestAmount;

    /**
     * 환불 사유
     */
    private String reason;


}
