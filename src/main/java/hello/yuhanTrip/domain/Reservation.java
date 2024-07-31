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

    private String reservationUid; // 예약번호

    @ManyToOne(fetch = FetchType.LAZY) // 여러개의 예약이 하나의 고객에게 연결 // 지연로딩
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToOne(fetch = FetchType.LAZY) // 1대1
    @JoinColumn(name = "payment_id")
    private Payment payment;



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
    private String specialRequests;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;


    @Column(name = "addr", nullable = false)
    private String addr;


    @Column(name = "price", nullable = false)
    private Long price; // 예약된 숙소의 총 가격






}
