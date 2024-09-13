package hello.yuhanTrip.controller.restApi;

import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.dto.kakao.KakaoRegisterRequest;
import hello.yuhanTrip.dto.kakao.KakaoUserInfoResponseDto;
import hello.yuhanTrip.dto.member.LoginDTO;
import hello.yuhanTrip.dto.token.TokenDTO;
import hello.yuhanTrip.repository.MemberRepository;
import hello.yuhanTrip.service.member.KakaoService;
import hello.yuhanTrip.service.member.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class KakaoLoginController {

    private final KakaoService kakaoService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;


    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code, HttpServletResponse response, HttpServletRequest request) {
        try {
            // 1. 카카오에서 AccessToken 가져오기
            String accessToken = kakaoService.getAccessTokenFromKakao(code);

            // 2. AccessToken을 사용하여 카카오 유저 정보 가져오기
            KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(accessToken);
            String kakaoId = userInfo.getId().toString();

            // 3. 회원 존재 여부 확인
            Member existingMember = memberRepository.findByAuthProviderId(kakaoId);

            // 4. 회원이 없으면 회원가입 처리
            if (existingMember == null) {
                memberService.registerKakaoUserOrLogin(userInfo);
                existingMember = memberRepository.findByAuthProviderId(kakaoId);  // 회원 가입 후 다시 조회
            }

            // 5. 이메일 정보가 없으면 이메일 입력 페이지로 리다이렉트
            if (existingMember.getEmail() == null) {
                String redirectUrl = String.format("/member/email/input?id=%s", kakaoId);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", redirectUrl)
                        .build();
            }

            LoginDTO loginDTO = new LoginDTO();
            LoginDTO loginDTO1 = loginDTO.builder()
                    .email(existingMember.getEmail())
                    .password("0000")
                    .build();
            TokenDTO tokenDTO = memberService.login(loginDTO1);

            // 6. 이메일 정보가 있으면 로그인 처리
            addCookie(response, "accessToken", tokenDTO.getAccessToken(), 3600);
            addCookie(response,"refreshToken",tokenDTO.getRefreshToken(),600*600);

            // 로그인 후 리다이렉트할 URL을 세션에 저장
            String previousUrl = (String) request.getSession().getAttribute("redirectUrl");
            if (previousUrl != null) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", previousUrl)
                        .build();
            }

            return ResponseEntity.ok(tokenDTO.getAccessToken());

        } catch (Exception e) {
            log.error("카카오 로그인 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카카오 로그인 실패");
        }
    }


    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(false); // https에서 작동하게 하려면 true로
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    @PostMapping("/kakaoRegister")
    public ResponseEntity<?> kakaoRegister(@RequestBody KakaoRegisterRequest request) {
        try {
            Long userInfoId = request.getId();
            String email = request.getEmail();

            Member member = memberRepository.findByAuthProviderId(userInfoId.toString());
            if (member == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("회원 정보가 없습니다.");
            }

            member.setEmail(email);
            memberRepository.save(member); // 변경된 이메일 저장

            return ResponseEntity.ok("회원 가입 완료");
        } catch (Exception e) {
            log.error("회원 가입 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 가입 처리 중 오류 발생");
        }
    }

}