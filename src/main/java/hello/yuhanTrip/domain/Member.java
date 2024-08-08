package hello.yuhanTrip.domain;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

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


    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CancelReservation> cancelReservations;


    public Member toMember(PasswordEncoder passwordEncoder) {
        // 회원 객체를 생성하고 반환
        return Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .memberRole(memberRole)
                .build();
    }

    public UsernamePasswordAuthenticationToken toAuthentication() {
        return new UsernamePasswordAuthenticationToken(email, password);
        // 사용자 로그인 기능 처리하기 위한 메서드
    }



}

