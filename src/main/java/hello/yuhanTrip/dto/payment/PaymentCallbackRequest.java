package hello.yuhanTrip.dto.payment;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;


// 결제가 이루어진 후 서버가 전달받는 데이터
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PaymentCallbackRequest {
    private String paymentUid; // 결제 고유 번호
    private String reservationUid; // 예약 고유 번호
}
