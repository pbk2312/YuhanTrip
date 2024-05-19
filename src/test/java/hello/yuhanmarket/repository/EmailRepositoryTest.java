package hello.yuhanmarket.repository;

import hello.yuhanmarket.domain.EmailCertification;
import hello.yuhanmarket.dto.email.EmailRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Random;

@SpringBootTest
class EmailRepositoryTest {

    private final EmailRepository emailRepository;

    @Autowired
    public EmailRepositoryTest(EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    @Test
    void saveAndRetrieve() {
        // 임의의 이메일 주소와 인증 번호 생성
        String certificationNumber = generateCertificationNumber();
        String email = "example@example.com";

        // EmailRequestDTO를 사용하여 EmailCertification 객체 생성
        EmailRequestDTO emailRequestDTO = EmailRequestDTO.builder()
                .email(email)
                .build();
        EmailCertification emailCertification = emailRequestDTO.toEmail(certificationNumber);

        // EmailCertification 객체 저장
        EmailCertification savedCertification = emailRepository.save(emailCertification);



    }

    private String generateCertificationNumber() {
        int length = 6; // 인증번호 길이
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int digit = random.nextInt(10); // 0부터 9까지의 난수 생성
            sb.append(digit);
        }
        return sb.toString();
    }
}
