package hello.yuhanmarket.service;


import hello.yuhanmarket.domain.EmailCertification;
import hello.yuhanmarket.dto.EmailRequestDTO;
import hello.yuhanmarket.email.EmailProvider;
import hello.yuhanmarket.repository.EmailRepository;
import hello.yuhanmarket.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Log4j2
public class EmailService {

    private final EmailRepository emailRepository;
    private final EmailProvider emailProvider;
    private final MemberRepository memberRepository;

    @Transactional
    public String sendCertificationMail(EmailRequestDTO emailRequestDTO) {


        // 인증번호가 일치하고, 해당 이메일로 가입된 회원이 있는지 확인
        if (memberRepository.existsByEmail(emailRequestDTO.getEmail())) {
            throw new RuntimeException("이미 가입되어 있는 회원입니다");
        }

        String certificationNumber = generateCertificationNumber();
        boolean emailsuccess = emailProvider.sendCertificationMail(emailRequestDTO.getEmail(), certificationNumber);
        if (!emailsuccess) {
            throw new RuntimeException("이메일 발송 실패");
        }
        EmailCertification email = emailRequestDTO.toEmail(certificationNumber);
        emailRepository.save(email);
        return "이메일 전송 성공";


    }

    @Transactional
    public String verifyEmail(String email, String certificationNumber) {

        EmailCertification emailCertification = emailRepository.findByCertificationEmail(email)
                .orElseThrow(() -> new RuntimeException("인증번호를 찾을 수 없습니다."));

        // 사용자가 입력한 인증번호와 DB에 저장된 인증번호를 비교합니다.
        if (!emailCertification.getCertificationNumber().equals(certificationNumber)) {
            throw new RuntimeException("인증번호가 일치하지 않습니다.");
        }



        // 인증이 성공했으므로 checkcertification 값을 true로 변경합니다.
        emailCertification.setCheckCertification(true);
        emailRepository.save(emailCertification); // 변경된 상태를 저장합니다.
        return "인증번호 인증 성공";


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
