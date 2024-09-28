package hello.yuhanTrip.controller.restApi;

import hello.yuhanTrip.domain.member.AuthProvider;
import hello.yuhanTrip.domain.member.ResetToken;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.dto.ResponseDTO;
import hello.yuhanTrip.dto.email.EmailRequestDTO;
import hello.yuhanTrip.dto.member.LoginDTO;
import hello.yuhanTrip.dto.member.LogoutDTO;
import hello.yuhanTrip.dto.member.WithdrawalMembershipDTO;
import hello.yuhanTrip.dto.register.MemberChangePasswordDTO;
import hello.yuhanTrip.dto.register.MemberRequestDTO;
import hello.yuhanTrip.dto.token.TokenDTO;
import hello.yuhanTrip.exception.EmailNotFoundException;
import hello.yuhanTrip.exception.IncorrectPasswordException;
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
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<ResponseDTO<String>> register(@RequestBody MemberRequestDTO memberRequestDTO) {
        try {
            String result = memberService.register(memberRequestDTO, AuthProvider.LOCAL);
            log.info("Registration result: {}", result);

            ResponseDTO<String> responseDTO = new ResponseDTO<>("회원가입이 성공적으로 완료되었습니다.", null);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (SpecificException e) {
            log.error("회원가입 중 특정 오류 발생: {}", e.getMessage());

            ResponseDTO<String> responseDTO = new ResponseDTO<>("회원가입 중 오류가 발생했습니다: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDTO);
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생: {}", e.getMessage());

            ResponseDTO<String> responseDTO = new ResponseDTO<>("회원가입 중 오류가 발생했습니다.", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
        }
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<Map<String, String>>> login(
            @RequestBody LoginDTO loginDTO,
            HttpServletResponse response,
            HttpServletRequest request) {

        log.info("로그인 요청...");

        Map<String, String> responseMap = new HashMap<>();

        try {
            // 이메일과 비밀번호를 검증한 후 토큰 반환
            TokenDTO tokenDTO = memberService.login(loginDTO);
            log.info("로그인이 완료되었습니다. 반환된 토큰: {}", tokenDTO);

            addCookie(response, "accessToken", tokenDTO.getAccessToken(), 3600); // 한시간
            addCookie(response, "refreshToken", tokenDTO.getRefreshToken(), 36000); // 7일

            String redirectUrl = (String) request.getSession().getAttribute("redirectUrl");
            request.getSession().removeAttribute("redirectUrl");

            responseMap.put("accessToken", tokenDTO.getAccessToken());
            responseMap.put("redirectUrl", redirectUrl != null ? redirectUrl : "/home/homepage");

            ResponseDTO<Map<String, String>> responseDTO = new ResponseDTO<>("로그인이 성공적으로 완료되었습니다.", responseMap);
            return ResponseEntity.ok(responseDTO);

        } catch (EmailNotFoundException e) {
            log.error("이메일을 찾을 수 없습니다: {}", loginDTO.getEmail());
            responseMap.put("error", "이메일을 찾을 수 없습니다.");

            ResponseDTO<Map<String, String>> responseDTO = new ResponseDTO<>("이메일을 찾을 수 없습니다.", responseMap);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDTO);

        } catch (IncorrectPasswordException e) {
            log.error("비밀번호가 잘못되었습니다.");
            responseMap.put("error", "비밀번호가 잘못되었습니다.");

            ResponseDTO<Map<String, String>> responseDTO = new ResponseDTO<>("비밀번호가 잘못되었습니다.", responseMap);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDTO);

        } catch (Exception e) {
            log.error("로그인 중 오류 발생: {}", e.getMessage());
            responseMap.put("error", "로그인 중 오류가 발생했습니다.");

            ResponseDTO<Map<String, String>> responseDTO = new ResponseDTO<>("로그인 중 오류가 발생했습니다.", responseMap);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
        }
    }


    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ResponseDTO<?>> logout(@CookieValue(value = "accessToken", required = false) String accessToken, HttpServletResponse response) {
        Member member = memberService.getUserDetails(accessToken);
        log.info("로그아웃 요청 - 유저: {}", member.getEmail());

        memberService.logout(new LogoutDTO(member.getEmail()));
        removeCookie(response, "accessToken");
        removeCookie(response, "refreshToken");

        log.info("로그아웃 완료");
        ResponseDTO<?> responseDTO = new ResponseDTO<>("로그아웃 완료", null);
        return ResponseEntity.ok(responseDTO);
    }


    @GetMapping("/validateToken")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> validateToken(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        Map<String, Object> data = new HashMap<>();

        try {
            // Access Token 검증
            if (accessToken != null && tokenProvider.validate(accessToken)) {
                data.put("isLoggedIn", true);
                return ResponseEntity.ok(new ResponseDTO<>("Access token is valid", data));
            }

            // Access Token이 만료된 경우 Refresh Token 확인
            if (refreshToken != null && tokenProvider.validate(refreshToken)) {
                Member member = memberService.findByRefreshToken(refreshToken);
                if (member != null) {
                    // 새 Access Token 발급
                    Authentication authentication = tokenProvider.getAuthenticationFromRefreshToken(refreshToken);
                    TokenDTO newTokenDTO = tokenProvider.generateTokenDto(authentication);

                    // 새 Access Token을 쿠키에 저장
                    addCookie(response, "accessToken", newTokenDTO.getAccessToken(), 3600); // 1시간 유효

                    data.put("isLoggedIn", true);
                    data.put("accessToken", newTokenDTO.getAccessToken());
                    return ResponseEntity.ok(new ResponseDTO<>("New access token issued", data));
                }
            }

            // Refresh Token이 유효하지 않은 경우
            data.put("isLoggedIn", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDTO<>("Refresh token is invalid", data));

        } catch (Exception e) {
            log.error("토큰 검증 중 오류 발생", e);
            data.put("isLoggedIn", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDTO<>("Error occurred during token validation", data));
        }
    }



    // 비밀번호 재설정 이메일
    @PostMapping("/sendResetPasswordEmail")
    public ResponseEntity<ResponseDTO<String>> sendResetPasswordEmail(@RequestBody EmailRequestDTO emailRequestDTO) {
        try {
            String message = memberService.sendPasswordResetEmail(emailRequestDTO);
            return ResponseEntity.ok(new ResponseDTO<>("이메일 전송이 완료되었습니다.", message));
        } catch (SpecificException e) {
            log.error("이메일 전송 중 특정 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO<>("이메일 전송 중 오류 발생: " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>("이메일 전송 실패: " + e.getMessage(), null));
        }
    }

    // 비밀번호 변경
    @PostMapping("/updatePassword")
    public ResponseEntity<ResponseDTO<String>> changePassword(@RequestParam("token") String resetToken,
                                                              @RequestParam("email") String email,
                                                              @RequestBody MemberChangePasswordDTO memberChangePasswordDTO) {
        try {
            validateResetToken(email, resetToken);
            memberChangePasswordDTO.setEmail(email);
            memberService.memberChangePassword(memberChangePasswordDTO);
            return ResponseEntity.ok(new ResponseDTO<>("비밀번호 변경이 완료되었습니다.", null));
        } catch (RuntimeException e) {
            log.error("비밀번호 변경 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO<>("비밀번호 변경 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }


    // 회원 탈퇴
    @PostMapping("/deleteAccount")
    public ResponseEntity<ResponseDTO<String>> deleteAccount(@RequestBody WithdrawalMembershipDTO withdrawalMembershipDTO,
                                                             @CookieValue(value = "accessToken", required = false) String accessToken,
                                                             HttpServletResponse response) {
        log.info("회원 탈퇴 요청...");
        Member member = memberService.getUserDetails(accessToken);
        withdrawalMembershipDTO.setEmail(member.getEmail());

        try {
            if (AuthProvider.KAKAO.equals(member.getAuthProvider())) {
                withdrawalMembershipDTO.setPassword("0000");
            }
            String message = memberService.deleteAccount(withdrawalMembershipDTO);
            if ("회원 정보가 정상적으로 삭제되었습니다.".equals(message)) {
                log.info("회원 정보 삭제 완료");
                removeCookie(response, "accessToken");
                return ResponseEntity.ok(new ResponseDTO<>("회원 탈퇴가 완료되었습니다.", null));
            } else {
                log.warn("회원 탈퇴 실패: {}", message);
                return ResponseEntity.badRequest()
                        .body(new ResponseDTO<>("회원 탈퇴 실패: " + message, null));
            }
        } catch (Exception e) {
            log.error("회원 탈퇴 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>("회원 탈퇴 중 오류가 발생했습니다.", null));
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
        cookie.setHttpOnly(true); // https 환경에서 사용하려면 true로
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