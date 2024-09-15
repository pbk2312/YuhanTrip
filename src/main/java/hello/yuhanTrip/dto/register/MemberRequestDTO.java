package hello.yuhanTrip.dto.register;

import hello.yuhanTrip.domain.member.AuthProvider;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.domain.member.MemberRole;
import lombok.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberRequestDTO {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String kakaoId;

    @NotBlank
    private String password;

    @NotBlank
    private String checkPassword;


    @NotBlank
    private String certificationNumber;

    @NotBlank
    private String name;

    @NotBlank
    private String nickname;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String address;

    @NotBlank
    private LocalDate dateOfBirth;


    @NotBlank
    private MemberRole memberRole;




    public Member toMember(PasswordEncoder passwordEncoder) {
        // 회원 객체를 생성하고 반환
        return Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name(name)  // 추가된 필드 반영
                .nickname(nickname)  // 추가된 필드 반영
                .phoneNumber(phoneNumber)  // 추가된 필드 반영
                .address(address)  // 추가된 필드 반영
                .dateOfBirth(dateOfBirth)  // 추가된 필드 반영
                .memberRole(MemberRole.ROLE_MEMBER)  // role도 DTO에서 직접 설정하도록 변경
                .authProvider(AuthProvider.LOCAL)
                .build();
    }



}
