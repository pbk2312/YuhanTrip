package hello.yuhanTrip.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CancelReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_cancel_id")
    private Long id;

    private String reservationUid; // 예약번호

    @ManyToOne(fetch = FetchType.LAZY) // 여러개의 취소 예약 내역이 하나의 고객에게 연결 // 지연로딩
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room; // Room과의 연관관계


    @Column(name = "accommodation_id", nullable = false)
    private Long accommodationId; // 숙소 ID


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

    @Column(name = "number_of_guests", nullable = false)
    private int numberOfGuests;

    @Column(name = "price", nullable = false)
    private Long price; // 숙소 총 가격


}
