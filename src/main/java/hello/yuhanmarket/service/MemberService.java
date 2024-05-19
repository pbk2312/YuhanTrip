package hello.yuhanmarket.service;

import hello.yuhanmarket.domain.Member;
import hello.yuhanmarket.dto.MemberRequestDTO;
import hello.yuhanmarket.repository.EmailRepository;
import hello.yuhanmarket.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final EmailRepository emailRepository;
    private final PasswordEncoder passwordEncoder;

    public String register(MemberRequestDTO memberRequestDTO) {
        String email = memberRequestDTO.getEmail();
        String password = memberRequestDTO.getPassword();
        String checkPassword = memberRequestDTO.getCheckPassword();


        log.info("회원가입 진행...");

        if (!password.equals(checkPassword)) {
            throw new RuntimeException("비밀번호 불일치");
        }

        // 이메일 인증 상태 확인
        boolean isEmailVerified = emailRepository.existsByCertificationEmailAndCheckCertificationIsTrue(email);
        if (!isEmailVerified) {
            throw new RuntimeException("이메일 인증이 완료되지 않았습니다.");
        }

        Member member = memberRequestDTO.toMember(passwordEncoder);

        memberRepository.save(member);


        return "회원가입 성공"; // 회원가입 성공 시 로그인 페이지로 리다이렉트
    }

}
