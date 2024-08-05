package hello.yuhanTrip.dto;

import lombok.*;

import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReservationUpdateDTO {

    private Long id; // 예약 ID
    private LocalDate checkInDate; // 체크인 날짜
    private LocalDate checkOutDate; // 체크아웃 날짜
    private int numberOfGuests; // 숙박 인원수

    private String addr; // 예약자 주소
    private String phoneNumber; // 고객 전화번호
    private String name; // 고객 이름
    private String specialRequests; // 특별 요청 사항
}
