package hello.yuhanTrip.dto.register;

import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.domain.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class MemberRequestDTO {

    @NotBlank
    @Email
    private String email;

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
                .memberRole(MemberRole.MEMBER)  // role도 DTO에서 직접 설정하도록 변경
                .build();
    }


    public UsernamePasswordAuthenticationToken toAuthentication() {
        return new UsernamePasswordAuthenticationToken(email, password);
        // 사용자 로그인 기능 처리하기 위한 메서드
    }

    // 추가: 인증번호를 DTO에 포함시키는 메서드
    public void setCertificationNumber(String certificationNumber) {
        this.certificationNumber = certificationNumber;
    }

}
