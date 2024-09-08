package hello.yuhanTrip.domain;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import hello.yuhanTrip.domain.accommodation.Accommodation;
import hello.yuhanTrip.domain.accommodation.Review;
import hello.yuhanTrip.domain.admin.RoleChangeRequest;
import hello.yuhanTrip.domain.reservation.Reservation;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private MemberRole memberRole;


    // 추가된 필드들
    private String name;
    private String nickname;  // 닉네임 필드 추가
    private String phoneNumber;
    private String address;
    private LocalDate dateOfBirth;  // 생일 필드를 LocalDate로 수정


    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Accommodation> accommodations; // 멤버가 등록한 숙소 리스트

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations;


    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberLike> memberLikes; // 추가된 부분


    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;  // 추가된 부분

    @OneToMany(mappedBy = "member")
    private List<RoleChangeRequest> roleChangeRequests;  // 추가된 부분

    @OneToMany(mappedBy = "member")
    private List<Coupon> coupons;

}

