package hello.yuhanTrip.service.member;

import hello.yuhanTrip.domain.member.AuthProvider;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.domain.member.MemberRole;
import hello.yuhanTrip.dto.register.MemberRequestDTO;
import hello.yuhanTrip.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;


@RequiredArgsConstructor
@Log4j2
public class KakaoMemberRegister implements MemberType {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;



    @Override
    public Member register(MemberRequestDTO memberRequestDTO) {
        log.info("카카오 회원 가입 진행...");
        String kakaoId = memberRequestDTO.getKakaoId();
        Member newMember = Member.builder()
                .authProviderId(kakaoId)
                .email(memberRequestDTO.getEmail())
                .name(memberRequestDTO.getName()) // 카카오 프로필 닉네임 사용
                .authProvider(AuthProvider.KAKAO)
                .memberRole(MemberRole.ROLE_MEMBER)
                .password(passwordEncoder.encode("0000"))
                .build();
        log.info("회원 가입 완료.... 이메일 : {} " ,memberRequestDTO.getEmail());
        memberRepository.save(newMember);
        return newMember;
    }
}
