package hello.yuhanTrip.controller.restApi;

import hello.yuhanTrip.domain.member.AuthProvider;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.domain.member.MemberRole;
import hello.yuhanTrip.dto.ResponseDTO;
import hello.yuhanTrip.dto.kakao.KakaoRegisterRequest;
import hello.yuhanTrip.dto.kakao.KakaoUserInfoResponseDto;
import hello.yuhanTrip.dto.member.LoginDTO;
import hello.yuhanTrip.dto.register.MemberRequestDTO;
import hello.yuhanTrip.dto.token.TokenDTO;
import hello.yuhanTrip.exception.KakaoApiException;
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

import java.util.Optional;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class KakaoLoginController {

    private final KakaoService kakaoService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @GetMapping("/callback")
    public ResponseEntity<ResponseDTO<?>> callback(
            @RequestParam("code") String code,
            HttpServletResponse response,
            HttpServletRequest request) {
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
                existingMember = registerNewMember(userInfo);
            }

            // 5. 이메일 정보가 없으면 이메일 입력 페이지로 리다이렉트
            if (existingMember.getEmail() == null) {
                String redirectUrl = String.format("/member/email/input?id=%s", kakaoId);
                ResponseDTO<String> responseDTO = new ResponseDTO<>("이메일 입력 페이지로 리다이렉트합니다.", null);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", redirectUrl)
                        .body(responseDTO);
            }

            // 6. 이메일 정보가 있으면 로그인 처리
            TokenDTO tokenDTO = handleLogin(existingMember);

            // 쿠키에 토큰 추가
            addCookie(response, "accessToken", tokenDTO.getAccessToken(), 3600, request);
            addCookie(response, "refreshToken", tokenDTO.getRefreshToken(), 600 * 600, request);

            // 로그인 후 리다이렉트할 URL을 세션에 저장
            Optional<String> redirectUrl = Optional.ofNullable((String) request.getSession().getAttribute("redirectUrl"));
            if (redirectUrl.isPresent()) {
                ResponseDTO<String> responseDTO = new ResponseDTO<>("리다이렉트합니다.", null);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", redirectUrl.get())
                        .body(responseDTO);
            } else {
                ResponseDTO<String> responseDTO = new ResponseDTO<>("로그인 성공", tokenDTO.getAccessToken());
                return ResponseEntity.ok(responseDTO);
            }

        } catch (KakaoApiException e) {
            log.error("카카오 API 호출 실패: {}", e.getMessage());
            ResponseDTO<String> responseDTO = new ResponseDTO<>("카카오 API 호출 실패", null);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(responseDTO);
        } catch (Exception e) {
            log.error("카카오 로그인 실패: {}", e.getMessage());
            ResponseDTO<String> responseDTO = new ResponseDTO<>("카카오 로그인 실패", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
        }
    }


    @PostMapping("/kakaoRegister")
    public ResponseEntity<ResponseDTO<?>> kakaoRegister(@RequestBody KakaoRegisterRequest request) {
        try {
            Long userInfoId = request.getId();
            String email = request.getEmail();

            Member member = memberRepository.findByAuthProviderId(userInfoId.toString());
            if (member == null) {
                ResponseDTO<String> responseDTO = new ResponseDTO<>("회원 정보가 없습니다.", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDTO);
            }

            member.setEmail(email);
            memberRepository.save(member); // 변경된 이메일 저장

            ResponseDTO<String> responseDTO = new ResponseDTO<>("회원 가입 완료", null);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            log.error("회원 가입 처리 중 오류 발생: {}", e.getMessage());
            ResponseDTO<String> responseDTO = new ResponseDTO<>("회원 가입 처리 중 오류 발생", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
        }
    }


    // 회원가입 처리 로직 분리
    private Member registerNewMember(KakaoUserInfoResponseDto userInfo) {
        MemberRequestDTO memberRequestDTO = MemberRequestDTO.builder()
                .kakaoId(userInfo.getId().toString())
                .email(userInfo.getKakaoAccount().getEmail())
                .name(userInfo.getKakaoAccount().getProfile().getNickName()) // 카카오 프로필 닉네임 사용
                .memberRole(MemberRole.ROLE_MEMBER)
                .build();
        memberService.register(memberRequestDTO, AuthProvider.KAKAO);
        return memberRepository.findByAuthProviderId(userInfo.getId().toString());
    }

    // 로그인 처리 로직 분리
    private TokenDTO handleLogin(Member existingMember) {
        LoginDTO loginDTO = LoginDTO.builder()
                .email(existingMember.getEmail())
                .password("0000") // 임시 비밀번호
                .build();
        return memberService.login(loginDTO);
    }

    // 쿠키 설정
    private void addCookie(HttpServletResponse response, String name, String value, int maxAge, HttpServletRequest request) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure()); // HTTPS 여부에 따라 쿠키의 보안 설정
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}