package hello.yuhanTrip.service.member;


import hello.yuhanTrip.domain.member.EmailCertification;
import hello.yuhanTrip.dto.email.EmailRequestDTO;
import hello.yuhanTrip.exception.CustomException;
import hello.yuhanTrip.repository.EmailRepository;
import hello.yuhanTrip.email.EmailProvider;
import hello.yuhanTrip.repository.MemberRepository;
import hello.yuhanTrip.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Log4j2
public class EmailService {

    private final EmailRepository emailRepository;
    private final EmailProvider emailProvider;
    private final MemberRepository memberRepository;
    private final RedisService redisService;

    @Transactional
    public String sendCertificationMail(EmailRequestDTO emailRequestDTO) {


        // 인증번호가 일치하고, 해당 이메일로 가입된 회원이 있는지 확인
        if (memberRepository.existsByEmail(emailRequestDTO.getEmail())) {
            throw new CustomException("이미 가입되어 있는 회원입니다");
        }

        String certificationNumber = generateCertificationNumber();
        boolean emailsuccess = emailProvider.sendCertificationMail(emailRequestDTO.getEmail(), certificationNumber);
        if (!emailsuccess) {
            throw new CustomException("이메일 발송 실패");
        }

        redisService.saveEmailCertificationToRedis(emailRequestDTO.getEmail(), certificationNumber); // 60분

        return "이메일 전송 성공";


    }

    @Transactional
    public String verifyEmail(String email, String certificationNumber) {
        String value = redisService.getEmailCertificationFromRedis(email);

        // Redis에서 가져온 값이 null인 경우 처리
        if (value == null) {
            throw new CustomException("인증번호가 만료되었거나 존재하지 않습니다.");
        }

        String[] parts = value.split(":");
        String storedCertificationNumber = parts[0]; // 저장된 인증번호
        boolean isVerified = Boolean.parseBoolean(parts[1]); // 현재 인증 상태

        // 사용자가 입력한 인증번호와 Redis에 저장된 인증번호 비교
        if (!storedCertificationNumber.equals(certificationNumber)) {
            throw new CustomException("인증번호가 일치하지 않습니다.");
        }

        if (isVerified) {
            throw new CustomException("이미 인증된 이메일입니다.");
        }

        // 인증 성공 시 상태 변경
        String updatedValue = storedCertificationNumber + ":true"; // 상태를 true로 변경
        redisService.updateEmailCertificationInRedis(email, updatedValue); // 업데이트 메서드 호출

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
