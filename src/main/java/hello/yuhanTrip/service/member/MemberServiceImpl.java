package hello.yuhanTrip.service.member;

import hello.yuhanTrip.domain.*;
import hello.yuhanTrip.domain.accommodation.Accommodation;
import hello.yuhanTrip.domain.member.AuthProvider;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.domain.member.MemberRole;
import hello.yuhanTrip.dto.LoginDTO;
import hello.yuhanTrip.dto.LogoutDTO;
import hello.yuhanTrip.dto.WithdrawalMembershipDTO;
import hello.yuhanTrip.dto.email.EmailRequestDTO;
import hello.yuhanTrip.dto.kakao.KakaoUserInfoResponseDto;
import hello.yuhanTrip.dto.register.MemberChangePasswordDTO;
import hello.yuhanTrip.dto.register.MemberRequestDTO;
import hello.yuhanTrip.dto.token.TokenDTO;
import hello.yuhanTrip.email.EmailProvider;
import hello.yuhanTrip.exception.InvalidHostException;
import hello.yuhanTrip.exception.SpecificException;
import hello.yuhanTrip.exception.UnauthorizedException;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.repository.EmailRepository;
import hello.yuhanTrip.repository.MemberRepository;
import hello.yuhanTrip.repository.RefreshTokenRepository;
import hello.yuhanTrip.repository.ResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;


@Service
@Log4j2
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final MemberRepository memberRepository;
    private final EmailRepository emailRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ResetTokenRepository resetTokenReposiotry;
    private final EmailProvider emailProvider;


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

        // 이메일로 인증번호 조회
        EmailCertification emailCertification = emailRepository.findByCertificationEmail(memberRequestDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("인증번호를 찾을 수 없습니다."));

        emailRepository.delete(emailCertification);
        member.setAuthProvider(AuthProvider.LOCAL);

        memberRepository.save(member);


        return "회원가입 성공"; // 회원가입 성공 시 로그인 페이지로 리다이렉트
    }

    // 카카오 회원가입
    public String registerKakaoUser(KakaoUserInfoResponseDto kakaoUserInfo) {
        String email = kakaoUserInfo.getKakaoAccount().getEmail();
        String nickname = kakaoUserInfo.getKakaoAccount().getProfile().getNickName();

        log.info("카카오 회원가입 진행...");

        if (email == null || email.isEmpty()) {
            throw new RuntimeException("이메일 정보가 필요합니다.");
        }

        boolean emailExists = memberRepository.existsByEmail(email);
        if (emailExists) {
            return "이미 가입된 이메일입니다.";
        }

        Member member = Member.builder()
                .email(email)
                .nickname(nickname)
                .authProvider(AuthProvider.KAKAO)
                .build();

        memberRepository.save(member);

        return "카카오 회원가입 성공";
    }

    @Transactional
    public TokenDTO login(LoginDTO loginDTO) {
        log.info("로그인 시도: 사용자 아이디={}", loginDTO.getEmail());

        // 1. 로그인 ID/PW를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = loginDTO.toAuthentication();

        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        log.info("사용자 인증 완료: 사용자 아이디={}", authentication.getName());

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        TokenDTO tokenDTO = tokenProvider.generateTokenDto(authentication);
        log.info("JWT 토큰 생성 완료");

        // 4. RefreshToken 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .email(loginDTO.getEmail())
                .key(authentication.getName())
                .value(tokenDTO.getRefreshToken())
                .build();
        refreshTokenRepository.save(refreshToken);
        log.info("RefreshToken 저장 완료: 사용자 아이디={}", authentication.getName());

        // 5. 토큰 발급
        log.info("로그인 완료: 사용자 아이디={}", authentication.getName());
        return tokenDTO;
    }

    @Transactional
    public void logout(LogoutDTO logoutDTO) {

        String email = logoutDTO.getEmail();
        refreshTokenRepository.deleteByEmail(email);

    }


    public String sendPasswordResetEmail(EmailRequestDTO emailRequestDTO) {

        // 회원 이메일 존재 여부 확인
        memberRepository.findByEmail(emailRequestDTO.getEmail())
                .orElseThrow(() -> new SpecificException("존재하지 않는 회원입니다."));

        // 임시 비밀번호 생성
        String resetToken = generateResetToken();

        // ResetToken 엔티티 생성 및 저장
        ResetToken tokenEntity = new ResetToken();
        tokenEntity.setEmail(emailRequestDTO.getEmail());
        tokenEntity.setResetToken(resetToken);
        resetTokenReposiotry.save(tokenEntity);

        // 비밀번호 재설정 링크
        String resetLink = "http://localhost:8080/member/updatePassword?token=" + resetToken + "&email=" + emailRequestDTO.getEmail();

        // 이메일 보내기
        boolean emailSent = emailProvider.sendPasswordResetEmail(emailRequestDTO, resetLink);
        if (!emailSent) {
            throw new SpecificException("이메일 발송에 실패했습니다.");
        }

        return "비밀번호 재설정 이메일 전송 성공";
    }

    // 비밀번호 재설정
    @Transactional
    public void memberChangePassword(MemberChangePasswordDTO memberChangePasswordDTO) {
        String email = memberChangePasswordDTO.getEmail();
        String password = memberChangePasswordDTO.getPassword();
        String checkPassword = memberChangePasswordDTO.getCheckPassword();

        if (!password.equals(checkPassword)) {
            throw new RuntimeException("입력한 비밀번호가 일치하지 않습니다.");
        }

        log.info("비밀번호 일치,비밀번호 인코딩");

        //비밀번호 암호화
        String hashedPassword = passwordEncoder.encode(password);

        log.info("비밀번호 암호화 완료");

        memberRepository.updatePasswordByEmail(email, hashedPassword);

        log.info("성공적으로 DB에 반영");
    }

    @Transactional
    public String deleteAccount(WithdrawalMembershipDTO withdrawalMembershipDTO) {
        Member member = memberRepository.findByEmail(withdrawalMembershipDTO.getEmail()).orElseThrow(() -> new RuntimeException("존재하지 않는 회원 입니다."));
        if (!passwordEncoder.matches(withdrawalMembershipDTO.getPassword(), member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");

        }
        // 해당 회원의 RefreshToken을 삭제합니다.
        refreshTokenRepository.deleteByEmail(member.getEmail());

        memberRepository.delete(member);

        log.info("회원 정보 삭제...");


        return "회원 정보가 정상적으로 삭제되었습니다.";

    }

    @Transactional
    public void updateMember(Member member) {
        memberRepository.updateMemberInfo(
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                member.getPhoneNumber(),
                member.getDateOfBirth(),
                member.getAddress()
        );
    }

    public Member validateHost(String accessToken) {

        Member member = getUserDetails(accessToken);

        if (member.getMemberRole() != MemberRole.ROLE_HOST) {
            throw new InvalidHostException("오직 호스트 등급만 숙소 등록을 할 수 있습니다.");
        }
        return member;
    }

    @Transactional(readOnly = true)
    public List<Accommodation> getAccommodationsByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + memberId));
        return member.getAccommodations();
    }



    // 회원 찾기
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user"));

    }


    // 임시 비밀번호 생성 메서드
    private String generateResetToken() {
        int length = 20; // 임시 비밀번호 길이
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            char randomChar = (char) (random.nextInt(26) + 'a'); // 알파벳 소문자 랜덤 생성
            sb.append(randomChar);
        }
        return sb.toString();
    }

    public Member getUserDetails(String accessToken) {
        if (isInvalidToken(accessToken)) {
            throw new UnauthorizedException("인증되지 않은 사용자입니다.");
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return findByEmail(userDetails.getUsername());
    }

    private boolean isInvalidToken(String accessToken) {
        return accessToken == null || !tokenProvider.validate(accessToken);
    }
}
