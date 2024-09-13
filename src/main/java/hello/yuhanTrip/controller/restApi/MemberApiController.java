package hello.yuhanTrip.controller.restApi;

import hello.yuhanTrip.domain.member.ResetToken;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.dto.member.LoginDTO;
import hello.yuhanTrip.dto.member.LogoutDTO;
import hello.yuhanTrip.dto.member.WithdrawalMembershipDTO;
import hello.yuhanTrip.dto.email.EmailRequestDTO;
import hello.yuhanTrip.dto.register.MemberChangePasswordDTO;
import hello.yuhanTrip.dto.register.MemberRequestDTO;
import hello.yuhanTrip.dto.token.TokenDTO;
import hello.yuhanTrip.exception.SpecificException;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.repository.ResetTokenRepository;
import hello.yuhanTrip.service.member.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/member")
@Log4j2
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;
    private final ResetTokenRepository resetTokenRepository;
    private final TokenProvider tokenProvider;

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody MemberRequestDTO memberRequestDTO) {
        try {
            String result = memberService.register(memberRequestDTO);
            log.info("Registration result: {}", result);
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 성공적으로 완료되었습니다.");
        } catch (SpecificException e) {
            log.error("회원가입 중 특정 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원가입 중 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원가입 중 오류가 발생했습니다.");
        }
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginDTO loginDTO, HttpServletResponse response, HttpServletRequest request) {
        log.info("로그인 요청...");
        TokenDTO tokenDTO = memberService.login(loginDTO);
        log.info("로그인이 완료되었습니다. 반환된 토큰: {}", tokenDTO);

        addCookie(response, "accessToken", tokenDTO.getAccessToken(), 3600); // 한시간
        addCookie(response,"refreshToken",tokenDTO.getRefreshToken(),36000); // 7일

        String redirectUrl = (String) request.getSession().getAttribute("redirectUrl");
        request.getSession().removeAttribute("redirectUrl");

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("accessToken", tokenDTO.getAccessToken());
        responseMap.put("redirectUrl", redirectUrl != null ? redirectUrl : "/home/homepage");

        return ResponseEntity.ok(responseMap);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@CookieValue(value = "accessToken", required = false) String accessToken, HttpServletResponse response) {
        Member member = memberService.getUserDetails(accessToken);
        log.info("로그아웃 요청 - 유저: {}", member.getEmail());

        memberService.logout(new LogoutDTO(member.getEmail()));
        removeCookie(response, "accessToken");

        log.info("로그아웃 완료");
        return ResponseEntity.ok("로그아웃 완료");
    }

    // 비밀번호 재설정 이메일
    @PostMapping("/sendResetPasswordEmail")
    public ResponseEntity<String> sendResetPasswordEmail(@RequestBody EmailRequestDTO emailRequestDTO) {
        try {
            String message = memberService.sendPasswordResetEmail(emailRequestDTO);
            return ResponseEntity.ok(message);
        } catch (SpecificException e) {
            log.error("이메일 전송 중 특정 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이메일 전송 중 오류 발생: " + e.getMessage());
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이메일 전송 실패: " + e.getMessage());
        }
    }

    // 비밀번호 변경
    @PostMapping("/updatePassword")
    public ResponseEntity<String> changePassword(@RequestParam("token") String resetToken,
                                                 @RequestParam("email") String email,
                                                 @RequestBody MemberChangePasswordDTO memberChangePasswordDTO) {
        try {
            validateResetToken(email, resetToken);
            memberChangePasswordDTO.setEmail(email);
            memberService.memberChangePassword(memberChangePasswordDTO);
            return ResponseEntity.ok("비밀번호 변경이 완료되었습니다.");
        } catch (RuntimeException e) {
            log.error("비밀번호 변경 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("비밀번호 변경 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 회원 탈퇴
    @PostMapping("/deleteAccount")
    public ResponseEntity<Void> deleteAccount(@RequestBody WithdrawalMembershipDTO withdrawalMembershipDTO,
                                              @CookieValue(value = "accessToken", required = false) String accessToken,
                                              HttpServletResponse response) {
        log.info("회원 탈퇴 요청...");
        Member member = memberService.getUserDetails(accessToken);
        withdrawalMembershipDTO.setEmail(member.getEmail());

        try {
            String message = memberService.deleteAccount(withdrawalMembershipDTO);
            if ("회원 정보가 정상적으로 삭제되었습니다.".equals(message)) {
                log.info("회원 정보 삭제 완료");
                removeCookie(response, "accessToken");
                return ResponseEntity.ok().build();
            } else {
                log.warn("회원 탈퇴 실패: {}", message);
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            log.error("회원 탈퇴 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }





    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = createCookie(name, value, maxAge);
        response.addCookie(cookie);
    }

    private void removeCookie(HttpServletResponse response, String name) {
        Cookie cookie = createCookie(name, null, 0);
        response.addCookie(cookie);
    }

    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(false); // https 환경에서 사용하려면 true로
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    private void validateResetToken(String email, String resetToken) {
        ResetToken storedToken = resetTokenRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("ResetToken을 찾을 수 없습니다."));

        if (!storedToken.getResetToken().equals(resetToken)) {
            throw new RuntimeException("유효하지 않은 ResetToken입니다.");
        }

        if (storedToken.getExpiryDate() != null && storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("ResetToken이 만료되었습니다.");
        }
    }
}