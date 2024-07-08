package hello.yuhanmarket.service;

import hello.yuhanmarket.domain.EmailCertification;
import hello.yuhanmarket.domain.Member;
import hello.yuhanmarket.domain.RefreshToken;
import hello.yuhanmarket.domain.ResetToken;
import hello.yuhanmarket.dto.LoginDTO;
import hello.yuhanmarket.dto.LogoutDTO;
import hello.yuhanmarket.dto.email.EmailRequestDTO;
import hello.yuhanmarket.dto.register.MemberChangePasswordDTO;
import hello.yuhanmarket.dto.register.MemberRequestDTO;
import hello.yuhanmarket.dto.token.TokenDTO;
import hello.yuhanmarket.email.EmailProvider;
import hello.yuhanmarket.jwt.TokenProvider;
import hello.yuhanmarket.repository.EmailRepository;
import hello.yuhanmarket.repository.MemberRepository;
import hello.yuhanmarket.repository.RefreshTokenRepository;
import hello.yuhanmarket.repository.ResetTokenReposiotry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final MemberRepository memberRepository;
    private final EmailRepository emailRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ResetTokenReposiotry resetTokenReposiotry;
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

        memberRepository.save(member);


        return "회원가입 성공"; // 회원가입 성공 시 로그인 페이지로 리다이렉트
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
    public String logout(LogoutDTO logoutDTO) {

        String email = logoutDTO.getEmail();
        refreshTokenRepository.deleteByEmail(email);

        return "로그아웃 되었습니다.";
    }

    public String sendPasswordResetEmail(EmailRequestDTO emailRequestDTO) {

        memberRepository.findByEmail(emailRequestDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원 입니다."));

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
            throw new RuntimeException("이메일 발송에 실패했습니다.");
        }

        return "비밀번호 재설정 이메일 전송 성공";
    }

    // 비밀번호 재설정
    @Transactional
    public String memberChangePassword(MemberChangePasswordDTO memberChangePasswordDTO) {
        String email = memberChangePasswordDTO.getEmail();
        String password = memberChangePasswordDTO.getPassword();
        String checkPassword = memberChangePasswordDTO.getCheckPassword();

        if (!password.equals(checkPassword)) {
            throw new RuntimeException("입력한 비밀번호가 일치하지 않습니다.");
        }

        //비밀번호 암호화
        String hashedPassword = passwordEncoder.encode(password);

        memberRepository.updatePasswordByEmail(email, hashedPassword);

        return "비밀번호 변경이 완료되었습니다.";
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

}
