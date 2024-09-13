package hello.yuhanTrip.dto.member;

import hello.yuhanTrip.domain.member.AuthProvider;
import hello.yuhanTrip.domain.member.MemberRole;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    private Long id;
    private String authProviderId;
    private String email;
    private String password; // 일반적으로 DTO에서 비밀번호는 제외하지만, 요청에 따라 포함할 수 있습니다.
    private MemberRole memberRole;
    private AuthProvider authProvider;
    private String name;
    private String nickname;
    private String phoneNumber;
    private String address;
    private LocalDate dateOfBirth;

}