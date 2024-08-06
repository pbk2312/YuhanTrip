package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.ResetToken;
import hello.yuhanTrip.dto.LoginDTO;
import hello.yuhanTrip.dto.LogoutDTO;
import hello.yuhanTrip.dto.WithdrawalMembershipDTO;
import hello.yuhanTrip.dto.email.EmailRequestDTO;
import hello.yuhanTrip.dto.register.MemberChangePasswordDTO;
import hello.yuhanTrip.dto.token.TokenDTO;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.repository.ResetTokenRepository;
import hello.yuhanTrip.service.MemberService;
import hello.yuhanTrip.dto.register.MemberRequestDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Log4j2
@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final ResetTokenRepository resetTokenReposiotry;
    private final TokenProvider tokenProvider;

    @GetMapping("/register")
    public String showRegisterForm(@ModelAttribute MemberRequestDTO memberRequestDTO) {
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute MemberRequestDTO memberRequestDTO, BindingResult bindingResult) {
        try {
            String result = memberService.register(memberRequestDTO);
            log.info("Registration result: {}", result);
            return "redirect:/member/login"; // 회원 등록 성공 시 로그인 페이지로 리다이렉트
        } catch (Exception e) {
            log.error("Error registering member: {}", e.getMessage());
            return "redirect:/member/error"; // 회원 등록 실패 시 에러 페이지로 리다이렉트
        }
    }

    @GetMapping("/login")
    public String showLogin(@ModelAttribute LoginDTO loginDTO) {
        return "login";
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        log.info("로그인 요청...");
        TokenDTO tokenDTO = memberService.login(loginDTO);
        log.info("로그인이 완료되었습니다. 반환된 토큰: {}", tokenDTO);

        // 쿠키에 accessToken 저장
        Cookie accessTokenCookie = new Cookie("accessToken", tokenDTO.getAccessToken());
        accessTokenCookie.setHttpOnly(true); // 클라이언트 측 스크립트에서 접근 불가
        accessTokenCookie.setSecure(true); // HTTPS에서만 전송
        accessTokenCookie.setPath("/"); // 모든 경로에서 쿠키 전송
        accessTokenCookie.setMaxAge(60 * 60); // 쿠키 유효 기간 설정 (60분)
        response.addCookie(accessTokenCookie);

        return ResponseEntity.ok(tokenDTO);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            HttpServletResponse response) {

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String username = userDetails.getUsername();
        LogoutDTO logoutDTO = new LogoutDTO();
        logoutDTO.setEmail(username);

        log.info("로그아웃 요청");
        memberService.logout(logoutDTO);
        // 쿠키에서 accessToken 삭제
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setHttpOnly(true); // 클라이언트 측 스크립트에서 접근 불가
        accessTokenCookie.setSecure(true); // HTTPS에서만 전송
        accessTokenCookie.setPath("/"); // 모든 경로에서 쿠키 전송
        accessTokenCookie.setMaxAge(0); // 쿠키 즉시 삭제
        response.addCookie(accessTokenCookie);
        log.info("토큰 삭제");

        log.info("로그아웃 완료");
        return ResponseEntity.ok("로그아웃 완료");
    }

    @GetMapping("/sendResetPasswordEmail")
    public String sendResetPasswordForm(Model model) {
        model.addAttribute("emailRequestDTO", new EmailRequestDTO());
        return "resetPasswordForm";
    }

    @PostMapping("/sendResetPasswordEmail")
    public String sendResetPasswordEmail(@RequestBody EmailRequestDTO emailRequestDTO, RedirectAttributes redirectAttributes) {
        try {
            String message = memberService.sendPasswordResetEmail(emailRequestDTO);
            redirectAttributes.addFlashAttribute("message", message); // Flash attribute for one-time display
            return "redirect:/member/login"; // 이메일 전송 성공 시 로그인 페이지로 리다이렉트
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage()); // 오류 메시지를 플래시 어트리뷰트에 추가
            return "redirect:/member/resetPasswordForm"; // 이메일 전송 실패 시 비밀번호 재설정 폼으로 리다이렉트
        }
    }

    @GetMapping("/updatePassword")
    public String updatePassword(@RequestParam("token") String resetToken,
                                 @RequestParam("email") String email,
                                 Model model) {

        // ResetToken을 검증합니다.
        ResetToken storedToken = resetTokenReposiotry.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("ResetToken을 찾을 수 없습니다."));

        // 저장된 토큰과 요청된 토큰이 일치하는지 확인합니다.
        if (!storedToken.getResetToken().equals(resetToken)) {
            throw new RuntimeException("유효하지 않은 ResetToken입니다.");
        }

        // 만료 여부를 검사합니다.
        LocalDateTime expiryDate = storedToken.getExpiryDate();
        if (expiryDate != null && expiryDate.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("ResetToken이 만료되었습니다.");
        }

        // ResetToken이 유효하고 만료되지 않았다면 비밀번호 재설정 페이지로 이동합니다.
        // 이메일과 토큰을 모델에 추가하여 비밀번호 재설정 페이지에 사용할 수 있도록 합니다.
        model.addAttribute("email", email);
        model.addAttribute("resetToken", resetToken);

        return "updatePassword";
    }

    @PostMapping("/updatePassword")
    public String changePassword(@RequestParam("token") String resetToken,
                                 @RequestParam("email") String email,
                                 @ModelAttribute MemberChangePasswordDTO memberChangePasswordDTO,
                                 RedirectAttributes redirectAttributes) {

        // ResetToken을 검증합니다.
        ResetToken storedToken = resetTokenReposiotry.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("ResetToken을 찾을 수 없습니다."));

        // 저장된 토큰과 요청된 토큰이 일치하는지 확인합니다.
        if (!storedToken.getResetToken().equals(resetToken)) {
            throw new RuntimeException("유효하지 않은 ResetToken입니다.");
        }

        // 만료 여부를 검사합니다.
        LocalDateTime expiryDate = storedToken.getExpiryDate();
        if (expiryDate != null && expiryDate.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("ResetToken이 만료되었습니다.");
        }

        // 비밀번호 변경을 수행합니다.
        memberChangePasswordDTO.setEmail(email); // 클라이언트가 보낸 JSON 데이터에서 이메일 설정
        memberService.memberChangePassword(memberChangePasswordDTO);

        // 비밀번호 변경 성공 메시지를 플래시 어트리뷰트에 추가하여 한 번만 사용할 수 있도록 합니다.
        redirectAttributes.addFlashAttribute("successMessage", "비밀번호 변경이 완료되었습니다.");

        // 비밀번호 변경 후 홈 페이지로 리다이렉트합니다.
        return "redirect:/home/homepage";
    }

    @GetMapping("/withdrawalMembership")
    public String getWithdrawalMembershipForm(@CookieValue(value = "accessToken", required = false) String accessToken,
                                              Model model) {
        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            return "redirect:/member/login";
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.info("회원 탈퇴 시도 유저 : {}", userDetails.getUsername());

        WithdrawalMembershipDTO withdrawalMembershipDTO = new WithdrawalMembershipDTO();
        model.addAttribute("withdrawalMembershipDTO", withdrawalMembershipDTO);

        return "withdrawalMembership";
    }

    @PostMapping("/deleteAccount")
    public ResponseEntity<Void> deleteAccount(@RequestBody WithdrawalMembershipDTO withdrawalMembershipDTO,
                                              @CookieValue(value = "accessToken", required = false) String accessToken,
                                              HttpServletResponse response) {
        log.info("회원 탈퇴를 진행합니다...");

        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            log.info("사용자가 인증되지 않았습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.info("회원탈퇴 이메일: {}", userDetails.getUsername());

        withdrawalMembershipDTO.setEmail(userDetails.getUsername());

        try {
            String message = memberService.deleteAccount(withdrawalMembershipDTO);

            if ("회원 정보가 정상적으로 삭제되었습니다.".equals(message)) {
                log.info("회원 정보가 정상적으로 삭제되었습니다.");

                // 쿠키에서 accessToken 삭제
                Cookie accessTokenCookie = new Cookie("accessToken", null);
                accessTokenCookie.setHttpOnly(true); // 클라이언트 측 스크립트에서 접근 불가
                accessTokenCookie.setSecure(true); // HTTPS에서만 전송
                accessTokenCookie.setPath("/"); // 모든 경로에서 쿠키 전송
                accessTokenCookie.setMaxAge(0); // 쿠키 즉시 삭제
                response.addCookie(accessTokenCookie);

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
}
