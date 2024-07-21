package hello.yuhanTrip.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Reservation {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "accommodation_id", nullable = false)
    private Accommodation accommodation;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "special_requests")
    private String specialRequests; // 요청 사항을 저장할 필드

    @Column(name = "name", nullable = false)
    private String name; // 고객의 이름을 저장할 필드

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber; // 고객의 전화번호를 저장할 필드

    @Column(name = "price", nullable = false)
    private BigDecimal price; // 예약된 숙소의 총 가격



}
