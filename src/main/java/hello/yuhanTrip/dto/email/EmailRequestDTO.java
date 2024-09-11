package hello.yuhanTrip.dto.email;

import hello.yuhanTrip.domain.member.EmailCertification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRequestDTO {

    @NotBlank
    @Email
    private String email;

    public EmailCertification toEmail(String emailCertificationNumber) {
        // 회원 객체를 생성하고 반환
        return EmailCertification.builder()
                .certificationEmail(email)
                .certificationNumber(emailCertificationNumber)
                .checkCertification(false)
                .build();
    }
}
