package hello.yuhanTrip.dto.payment;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PaymentDTO {


    private String reservationUid; // 예약 고유 번호
    private Long memberId; // 회원 ID
    private Long accommodationId; // 숙소 ID
    private String accommodationTitle; // 숙소 제목
    private LocalDate reservationDate; // 예약 날짜
    private LocalDate checkInDate; // 체크인 날짜
    private LocalDate checkOutDate; // 체크아웃 날짜
    private String email; // 이메일
    private String addr; // 예약자 주소
    private String buyerName; // 고객 이름
    private String phoneNumber; // 고객 전화번호
    private String specialRequests; // 특별 요청 사항
    private Long totalPrice; // 총 가격
    private int numberOfGuests; // 숙박 인원수



}
