package hello.yuhanTrip.service.member;

import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.dto.register.MemberRequestDTO;
import hello.yuhanTrip.repository.MemberRepository;
import hello.yuhanTrip.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
public class LocalMemberRegister implements MemberType {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final RedisService redisService;
    // 필요한 생성자 정의


    @Override
    public Member register(MemberRequestDTO memberRequestDTO) {
        // 회원가입 로직 구현
        String email = memberRequestDTO.getEmail();
        String password = memberRequestDTO.getPassword();
        String checkPassword = memberRequestDTO.getCheckPassword();

        if (!password.equals(checkPassword)) {
            throw new RuntimeException("비밀번호 불일치");
        }

        // Redis에서 인증 상태 확인
        String value = redisService.getEmailCertificationFromRedis(email);
        if (value == null) {
            throw new RuntimeException("이메일 인증이 완료되지 않았습니다."); // 인증 정보가 없을 경우
        }

        String[] parts = value.split(":");
        boolean isEmailVerified = Boolean.parseBoolean(parts[1]); // 인증 상태 확인

        if (!isEmailVerified) {
            throw new RuntimeException("이메일 인증이 완료되지 않았습니다."); // 인증 상태가 false일 경우
        }

        // 새로운 Member 객체 생성 및 저장
        Member member = memberRequestDTO.toMember(passwordEncoder);



        return memberRepository.save(member);
    }






}