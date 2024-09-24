package hello.yuhanTrip.dto.accommodation;

import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReservationDTO {
    private Long id; // 예약 ID
    private Long memberId; // 회원 ID
    private Long accommodationId; // 숙소 ID
    private Long roomId; // 객실 ID
    private String roomNm; // 객실 번호
    private String roomType;// 객실 타입
    private LocalDate checkInDate; // 체크인 날짜
    private LocalDate checkOutDate; // 체크아웃 날짜
    private LocalDate reservationDate; // 예약 날짜
    private String specialRequests; // 특별 요청 사항
    private String accommodationTitle; // 숙소 제목
    private LocalDate localDate;
    private String name; // 고객 이름
    private String phoneNumber; // 고객 전화번호
    private Long price; //  1박 가격
    private String addr; // 예약자 주소
    private int numberOfGuests; // 숙박 인원수
    private String couponCode;


}
