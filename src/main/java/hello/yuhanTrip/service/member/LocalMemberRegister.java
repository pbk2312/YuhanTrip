package hello.yuhanTrip.service.member;

import hello.yuhanTrip.domain.member.EmailCertification;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.dto.register.MemberRequestDTO;
import hello.yuhanTrip.repository.EmailRepository;
import hello.yuhanTrip.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

public class LocalMemberRegister implements MemberType {

    private final PasswordEncoder passwordEncoder;
    private final EmailRepository emailRepository;
    private final MemberRepository memberRepository;

    // 필요한 생성자 정의
    public LocalMemberRegister(PasswordEncoder passwordEncoder, EmailRepository emailRepository, MemberRepository memberRepository) {
        this.passwordEncoder = passwordEncoder;
        this.emailRepository = emailRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public Member register(MemberRequestDTO memberRequestDTO) {
        // 회원가입 로직 구현
        String email = memberRequestDTO.getEmail();
        String password = memberRequestDTO.getPassword();
        String checkPassword = memberRequestDTO.getCheckPassword();

        if (!password.equals(checkPassword)) {
            throw new RuntimeException("비밀번호 불일치");
        }

        boolean isEmailVerified = emailRepository.existsByCertificationEmailAndCheckCertificationIsTrue(email);
        if (!isEmailVerified) {
            throw new RuntimeException("이메일 인증이 완료되지 않았습니다.");
        }

        Member member = memberRequestDTO.toMember(passwordEncoder);

        EmailCertification emailCertification = emailRepository.findByCertificationEmail(email)
                .orElseThrow(() -> new RuntimeException("인증번호를 찾을 수 없습니다."));

        emailRepository.delete(emailCertification);

        return memberRepository.save(member);
    }


    private void validatePassword(String password, String checkPassword) {
        if (!password.equals(checkPassword)) {
            throw new RuntimeException("비밀번호 불일치");
        }
    }

    private void checkEmailVerification(String email) {
        boolean isEmailVerified = emailRepository.existsByCertificationEmailAndCheckCertificationIsTrue(email);
        if (!isEmailVerified) {
            throw new RuntimeException("이메일 인증이 완료되지 않았습니다.");
        }
    }

    private void removeEmailCertification(String email) {
        EmailCertification emailCertification = emailRepository.findByCertificationEmail(email)
                .orElseThrow(() -> new RuntimeException("인증번호를 찾을 수 없습니다."));
        emailRepository.delete(emailCertification);
    }
}