package hello.yuhanTrip.controller.view;

import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.domain.member.ResetToken;
import hello.yuhanTrip.dto.email.EmailRequestDTO;
import hello.yuhanTrip.dto.member.LoginDTO;
import hello.yuhanTrip.dto.member.WithdrawalMembershipDTO;
import hello.yuhanTrip.dto.register.MemberRequestDTO;
import hello.yuhanTrip.repository.ResetTokenRepository;
import hello.yuhanTrip.service.member.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


@Log4j2
@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberViewController {

    private final MemberService memberService;
    private final ResetTokenRepository resetTokenRepository;


    @Value("${kakao.client_id}")
    private String client_id;

    @Value("${kakao.redirect_uri}")
    private String redirect_uri;

    // 회원가입
    @GetMapping("/register")
    public String showRegisterForm(@ModelAttribute MemberRequestDTO memberRequestDTO) {
        return "member/register";
    }


    // 로그인
    @GetMapping("/login")
    public String showLogin(HttpServletRequest request, @ModelAttribute LoginDTO loginDTO,Model model) {
        // Referer 헤더에서 원래 페이지 URL 추출
        String refererUrl = request.getHeader("Referer");
        if (refererUrl != null) {
            request.getSession().setAttribute("redirectUrl", refererUrl);
        }
        String location = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id="+client_id+"&redirect_uri="+redirect_uri;
        model.addAttribute("location", location);

        return "member/login";
    }



    // 비밀번호 재설정
    @GetMapping("/sendResetPasswordEmail")
    public String sendResetPasswordForm(Model model) {
        model.addAttribute("emailRequestDTO", new EmailRequestDTO());
        return "member/resetPasswordForm";
    }



    // 비밀번호 재설정
    @GetMapping("/updatePassword")
    public String updatePassword(@RequestParam("token") String resetToken, @RequestParam("email") String email, Model model) {
        validateResetToken(email, resetToken);

        model.addAttribute("email", email);
        model.addAttribute("resetToken", resetToken);

        return "member/updatePassword";
    }



    // 회원 탈퇴
    @GetMapping("/withdrawalMembership")
    public String getWithdrawalMembershipForm(@CookieValue(value = "accessToken", required = false) String accessToken, Model model) {
        Member member = memberService.getUserDetails(accessToken);
        log.info("탈퇴 시도 유저 : {}", member.getEmail());
        model.addAttribute("withdrawalMembershipDTO", new WithdrawalMembershipDTO());

        return "member/withdrawalMembership";
    }






    @GetMapping("/email/input")
    public String showEmailInputPage(@RequestParam("id") Long userInfoId, Model model) {
        // userInfoId를 모델에 추가
        model.addAttribute("userInfoId", userInfoId);

        // 이메일 입력 폼을 보여주는 HTML 페이지
        return "emailInput";
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
