package hello.yuhanmarket.controller;

import hello.yuhanmarket.domain.ResetToken;
import hello.yuhanmarket.dto.LoginDTO;
import hello.yuhanmarket.dto.LogoutDTO;
import hello.yuhanmarket.dto.email.EmailRequestDTO;
import hello.yuhanmarket.dto.register.MemberChangePasswordDTO;
import hello.yuhanmarket.dto.register.MemberRequestDTO;
import hello.yuhanmarket.dto.token.TokenDTO;
import hello.yuhanmarket.repository.ResetTokenReposiotry;
import hello.yuhanmarket.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
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
    private final ResetTokenReposiotry resetTokenReposiotry;

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
    public ResponseEntity<TokenDTO> login(@RequestBody LoginDTO loginDTO) {
        log.info("로그인 요청...");
        TokenDTO tokenDTO = memberService.login(loginDTO);
        log.info("로그인이 완료되었습니다. 반환된 토큰: {}", tokenDTO);
        return ResponseEntity.ok(tokenDTO);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody LogoutDTO logoutDTO) {
        log.info("로그아웃 요청");
        memberService.logout(logoutDTO);
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
                                 @RequestParam("email") String email) {

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
        return "updatePassword";

    }

    @PostMapping("/updatePassword/{email}")
    public String changePassword(@PathVariable("email") String email, @RequestBody MemberChangePasswordDTO memberChangePasswordDTO) {
        memberChangePasswordDTO.setEmail(email); // 이메일 설정은 클라이언트가 보낸 JSON 데이터에서 설정
        memberService.memberChangePassword(memberChangePasswordDTO);
        // 비밀번호 변경 성공 후 리다이렉트
        return "redirect:/home/homepage";
    }

}
