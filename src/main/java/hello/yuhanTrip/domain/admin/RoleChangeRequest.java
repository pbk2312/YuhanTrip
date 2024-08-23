package hello.yuhanTrip.domain.admin;


import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.domain.MemberRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private MemberRole requestedRole;

    private LocalDateTime requestDate;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;


}
