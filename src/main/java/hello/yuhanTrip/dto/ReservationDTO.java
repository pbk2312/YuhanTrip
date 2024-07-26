package hello.yuhanTrip.dto;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.domain.Reservation;
import lombok.*;

import java.math.BigDecimal;
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
    private LocalDate checkInDate; // 체크인 날짜
    private LocalDate checkOutDate; // 체크아웃 날짜
    private LocalDate reservationDate; // 예약 날짜
    private String specialRequests; // 특별 요청 사항
    private String accommodationTitle; // 숙소 제목
    private String accommodationAddr1; // 숙소 주소 1
    private String accommodationAddr2; // 숙소 주소 2
    private String name; // 고객 이름
    private String phoneNumber; // 고객 전화번호
    private BigDecimal price; //  1박 가격

    // 엔티티에서 DTO로 변환하는 메서드
    public static ReservationDTO fromEntity(Reservation reservation) {
        return ReservationDTO.builder()
                .id(reservation.getId())
                .memberId(reservation.getMember().getId())
                .accommodationId(reservation.getAccommodation().getId())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .reservationDate(reservation.getReservationDate())
                .specialRequests(reservation.getSpecialRequests())
                .name(reservation.getName())
                .phoneNumber(reservation.getPhoneNumber())
                .price(reservation.getPrice())
                .build();
    }

    // DTO에서 엔티티로 변환하는 메서드
    public static Reservation toEntity(ReservationDTO dto, Member member, Accommodation accommodation) {
        return Reservation.builder()
                .id(dto.getId())
                .member(member)
                .accommodation(accommodation)
                .checkInDate(dto.getCheckInDate())
                .checkOutDate(dto.getCheckOutDate())
                .reservationDate(dto.getReservationDate())
                .specialRequests(dto.getSpecialRequests())
                .name(dto.getName())
                .phoneNumber(dto.getPhoneNumber())
                .price(dto.getPrice())
                .build();
    }
}
