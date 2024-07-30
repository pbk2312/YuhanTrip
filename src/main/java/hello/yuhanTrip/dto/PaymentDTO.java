package hello.yuhanTrip.dto;


import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PaymentDTO {


    private Long reservationId; // 예약 ID
    private Long memberId; // 회원 ID
    private Long accommodationId; // 숙소 ID
    private String accommodationTitle; // 숙소 제목
    private LocalDate reservationDate; // 예약 날짜
    private LocalDate checkInDate; // 체크인 날짜
    private LocalDate checkOutDate; // 체크아웃 날짜
    private String name; // 고객 이름
    private String phoneNumber; // 고객 전화번호
    private String specialRequests; // 특별 요청 사항
    private int totalPrice; // 총 가격



}
