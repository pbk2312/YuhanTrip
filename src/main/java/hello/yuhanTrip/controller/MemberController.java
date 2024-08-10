package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.ResetToken;
import hello.yuhanTrip.dto.*;
import hello.yuhanTrip.dto.email.EmailRequestDTO;
import hello.yuhanTrip.dto.register.MemberChangePasswordDTO;
import hello.yuhanTrip.dto.register.MemberRequestDTO;
import hello.yuhanTrip.dto.token.TokenDTO;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.repository.ResetTokenRepository;
import hello.yuhanTrip.service.MemberService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Log4j2
@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final ResetTokenRepository resetTokenRepository;
    private final TokenProvider tokenProvider;

    @GetMapping("/register")
    public String showRegisterForm(@ModelAttribute MemberRequestDTO memberRequestDTO) {
        return "/member/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute MemberRequestDTO memberRequestDTO) {
        try {
            String result = memberService.register(memberRequestDTO);
            log.info("Registration result: {}", result);
            return "redirect:/member/login";
        } catch (Exception e) {
            log.error("Error registering member: {}", e.getMessage());
            return "redirect:/member/error";
        }
    }

    @GetMapping("/login")
    public String showLogin(@ModelAttribute LoginDTO loginDTO) {
        return "/member/login";
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        log.info("로그인 요청...");
        TokenDTO tokenDTO = memberService.login(loginDTO);
        log.info("로그인이 완료되었습니다. 반환된 토큰: {}", tokenDTO);

        addCookie(response, "accessToken", tokenDTO.getAccessToken(), 60 * 60);

        return ResponseEntity.ok(tokenDTO);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@CookieValue(value = "accessToken", required = false) String accessToken, HttpServletResponse response) {
        if (isInvalidToken(accessToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetails userDetails = getUserDetails(accessToken);
        log.info("로그아웃 요청 - 유저: {}", userDetails.getUsername());

        memberService.logout(new LogoutDTO(userDetails.getUsername()));
        removeCookie(response, "accessToken");

        log.info("로그아웃 완료");
        return ResponseEntity.ok("로그아웃 완료");
    }

    @GetMapping("/sendResetPasswordEmail")
    public String sendResetPasswordForm(Model model) {
        model.addAttribute("emailRequestDTO", new EmailRequestDTO());
        return "/member/resetPasswordForm";
    }

    @PostMapping("/sendResetPasswordEmail")
    public String sendResetPasswordEmail(@RequestBody EmailRequestDTO emailRequestDTO, RedirectAttributes redirectAttributes) {
        try {
            String message = memberService.sendPasswordResetEmail(emailRequestDTO);
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/member/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/resetPasswordForm";
        }
    }

    @GetMapping("/updatePassword")
    public String updatePassword(@RequestParam("token") String resetToken, @RequestParam("email") String email, Model model) {
        validateResetToken(email, resetToken);

        model.addAttribute("email", email);
        model.addAttribute("resetToken", resetToken);

        return "/member/updatePassword";
    }

    @PostMapping("/updatePassword")
    public String changePassword(@RequestParam("token") String resetToken, @RequestParam("email") String email,
                                 @ModelAttribute MemberChangePasswordDTO memberChangePasswordDTO, RedirectAttributes redirectAttributes) {
        validateResetToken(email, resetToken);

        memberChangePasswordDTO.setEmail(email);
        memberService.memberChangePassword(memberChangePasswordDTO);

        redirectAttributes.addFlashAttribute("successMessage", "비밀번호 변경이 완료되었습니다.");
        return "redirect:/home/homepage";
    }

    @GetMapping("/withdrawalMembership")
    public String getWithdrawalMembershipForm(@CookieValue(value = "accessToken", required = false) String accessToken, Model model) {
        if (isInvalidToken(accessToken)) {
            return "redirect:/member/login";
        }

        UserDetails userDetails = getUserDetails(accessToken);
        log.info("회원 탈퇴 시도 유저 : {}", userDetails.getUsername());

        model.addAttribute("withdrawalMembershipDTO", new WithdrawalMembershipDTO());

        return "/member/withdrawalMembership";
    }

    @PostMapping("/deleteAccount")
    public ResponseEntity<Void> deleteAccount(@RequestBody WithdrawalMembershipDTO withdrawalMembershipDTO,
                                              @CookieValue(value = "accessToken", required = false) String accessToken,
                                              HttpServletResponse response) {
        log.info("회원 탈퇴를 진행합니다...");

        if (isInvalidToken(accessToken)) {
            log.info("사용자가 인증되지 않았습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetails userDetails = getUserDetails(accessToken);
        withdrawalMembershipDTO.setEmail(userDetails.getUsername());

        try {
            String message = memberService.deleteAccount(withdrawalMembershipDTO);

            if ("회원 정보가 정상적으로 삭제되었습니다.".equals(message)) {
                log.info("회원 정보가 정상적으로 삭제되었습니다.");
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

    private boolean isInvalidToken(String accessToken) {
        return accessToken == null || !tokenProvider.validate(accessToken);
    }

    private UserDetails getUserDetails(String accessToken) {
        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        return (UserDetails) authentication.getPrincipal();
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private void removeCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
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
