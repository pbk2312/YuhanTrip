package hello.yuhanTrip.service.member;

import hello.yuhanTrip.domain.accommodation.Accommodation;
import hello.yuhanTrip.domain.member.*;
import hello.yuhanTrip.dto.member.LoginDTO;
import hello.yuhanTrip.dto.member.LogoutDTO;
import hello.yuhanTrip.dto.member.WithdrawalMembershipDTO;
import hello.yuhanTrip.dto.email.EmailRequestDTO;
import hello.yuhanTrip.dto.payment.MypageMemberDTO;
import hello.yuhanTrip.dto.register.MemberChangePasswordDTO;
import hello.yuhanTrip.dto.register.MemberRequestDTO;
import hello.yuhanTrip.dto.token.TokenDTO;
import hello.yuhanTrip.email.EmailProvider;
import hello.yuhanTrip.exception.*;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.repository.EmailRepository;
import hello.yuhanTrip.repository.MemberRepository;
import hello.yuhanTrip.repository.ResetTokenRepository;
import hello.yuhanTrip.service.RedisService;
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
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final ResetTokenRepository resetTokenReposiotry;
    private final EmailProvider emailProvider;
    private final EmailRepository emailRepository;
    private final RedisService redisService;

    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7일


    public String register(MemberRequestDTO memberRequestDTO, AuthProvider authProvider) {
        MemberType memberType = getMemberType(authProvider);
        Member member = memberType.register(memberRequestDTO);
        log.info("회원 가입 :  {} ", member.getName());
        return "회원 가입 완료";
    }

    private MemberType getMemberType(AuthProvider authProvider) {
        switch (authProvider) {
            case LOCAL:
                return new LocalMemberRegister(passwordEncoder, emailRepository, memberRepository);
            case KAKAO:
                return new KakaoMemberRegister(passwordEncoder, memberRepository);
            default:
                throw new IllegalArgumentException("지원하지 않는 인증 제공자입니다.");
        }
    }


    @Transactional
    public TokenDTO login(LoginDTO loginDTO) {
        log.info("로그인 시도: 사용자 아이디={}", loginDTO.getEmail());
        Member member = findByEmail(loginDTO.getEmail());
        validatePassword(loginDTO.getPassword(), member.getPassword());

        // 1. 로그인 ID/PW를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = loginDTO.toAuthentication();

        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        log.info("사용자 인증 완료: 사용자 아이디={}", authentication.getName());

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        TokenDTO tokenDTO = tokenProvider.generateTokenDto(authentication);
        log.info("JWT 토큰 생성 완료");

        // 4. Redis에 RefreshToken 저장
        redisService.setStringValue(String.valueOf(member.getId()), tokenDTO.getRefreshToken(), REFRESH_TOKEN_EXPIRE_TIME);
        log.info("Redis에 RefreshToken 저장 완료: 사용자 아이디={}", authentication.getName());

        // 5. 토큰 발급
        log.info("로그인 완료: 사용자 아이디={}", authentication.getName());
        return tokenDTO;
    }

    @Transactional
    public void logout(LogoutDTO logoutDTO) {
        log.info("로그아웃 시도: 사용자 아이디={}", logoutDTO.getEmail());

        // 이메일로 멤버 찾기
        Member member = findByEmail(logoutDTO.getEmail());

        // Redis에서 Refresh Token 삭제
        redisService.deleteStringValue(String.valueOf(member.getId()));
        log.info("Redis에서 RefreshToken 삭제 완료: 사용자 아이디={}", logoutDTO.getEmail());
    }

    public String sendPasswordResetEmail(EmailRequestDTO emailRequestDTO) {

        // 회원 이메일 존재 여부 확인
        memberRepository.findByEmail(emailRequestDTO.getEmail())
                .orElseThrow(() -> new EmailNotFoundException("존재하지 않는 회원입니다."));

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
    public Member findByRefreshToken(String refreshToken) {
        // Redis에서 refreshToken으로 memberId 찾기
        String memberId = redisService.findMemberIdByRefreshToken(refreshToken);
        if (memberId != null) {
            // memberId로 Member 정보 조회
            return memberRepository.findById(Long.valueOf(memberId))
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 사용자를 찾을 수 없습니다."));
        }
        throw new IllegalArgumentException("유효하지 않은 refreshToken입니다.");
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
        validatePassword(withdrawalMembershipDTO.getPassword(), member.getPassword());
        // 해당 회원의 RefreshToken을 삭제합니다.

        memberRepository.delete(member);

        log.info("회원 정보 삭제...");


        return "회원 정보가 정상적으로 삭제되었습니다.";

    }

    @Transactional
    public void updateMember(Member member, MypageMemberDTO mypageMemberDTO) {
        // 개인정보 수정
        member.setName(mypageMemberDTO.getName());
        member.setNickname(mypageMemberDTO.getNickname());
        member.setPhoneNumber(mypageMemberDTO.getPhoneNumber());
        member.setDateOfBirth(mypageMemberDTO.getDateOfBirth());
        member.setAddress(mypageMemberDTO.getAddress());

        // 저장 (업데이트)
        memberRepository.save(member);
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
                .orElseThrow(() -> new EmailNotFoundException("이메일을 찾을 수 없습니다."));

    }

    public void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new IncorrectPasswordException("비밀번호가 일치하지 않습니다.");
        }
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
