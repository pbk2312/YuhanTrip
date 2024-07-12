package hello.yuhanmarket.controller;

import hello.yuhanmarket.domain.ResetToken;
import hello.yuhanmarket.dto.LoginDTO;
import hello.yuhanmarket.dto.LogoutDTO;
import hello.yuhanmarket.dto.WithdrawalMembershipDTO;
import hello.yuhanmarket.dto.email.EmailRequestDTO;
import hello.yuhanmarket.dto.register.MemberChangePasswordDTO;
import hello.yuhanmarket.dto.register.MemberRequestDTO;
import hello.yuhanmarket.dto.token.TokenDTO;
import hello.yuhanmarket.repository.ResetTokenReposiotry;
import hello.yuhanmarket.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public String withdrawalMembershipForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        WithdrawalMembershipDTO withdrawalMembershipDTO = new WithdrawalMembershipDTO();
        model.addAttribute("withdrawalMembershipDTO", withdrawalMembershipDTO);

        return "withdrawalMembership";
    }


    @PostMapping("/deleteAccount")
    public ResponseEntity<Void> deleteAccount(@RequestBody WithdrawalMembershipDTO withdrawalMembershipDTO,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        // 사용자 인증 상태 확인
        if (userDetails == null) {
            log.info("사용자가 인증되지 않았습니다.");
            // 사용자가 인증되지 않은 경우 처리
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 로그인된 사용자의 이메일 확인
        log.info("회원탈퇴 이메일: {}", userDetails.getUsername());

        // WithdrawalMembershipDTO에 이메일 설정
        withdrawalMembershipDTO.setEmail(userDetails.getUsername());

        try {
            // 계정 삭제 서비스 호출
            String message = memberService.deleteAccount(withdrawalMembershipDTO);

            // 계정 삭제 결과에 따른 응답 처리
            if ("회원 정보가 정상적으로 삭제되었습니다.".equals(message)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            log.error("회원 탈퇴 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
