package hello.yuhanTrip.controller;

import hello.yuhanTrip.dto.WithdrawalMembershipDTO;
import hello.yuhanTrip.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageController {

    private final MemberService memberService;

    @GetMapping("/withdrawalMembership")
    public String withdrawalMembershipForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        log.info("로그인된 사용자: {}", userDetails.getUsername());
        WithdrawalMembershipDTO withdrawalMembershipDTO = new WithdrawalMembershipDTO();
        withdrawalMembershipDTO.setEmail(userDetails.getUsername());
        model.addAttribute("withdrawalMembershipDTO", withdrawalMembershipDTO);

        return "withdrawalMembership"; // HTML 템플릿 이름
    }

    @PostMapping("/deleteAccount")
    public String deleteAccount(
            @ModelAttribute WithdrawalMembershipDTO withdrawalMembershipDTO,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        log.info("탈퇴 시도 사용자 : {} ", userDetails.getUsername());

        // 현재 인증된 사용자의 이메일을 DTO에 설정
        String email = userDetails.getUsername();
        String password = withdrawalMembershipDTO.getPassword();

        withdrawalMembershipDTO.setEmail(email);
        withdrawalMembershipDTO.setPassword(password);

        // 계정 삭제 처리
        try {
            memberService.deleteAccount(withdrawalMembershipDTO);
            redirectAttributes.addFlashAttribute("message", "계정이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            log.error("계정 삭제 실패", e);
            redirectAttributes.addFlashAttribute("message", "계정 삭제에 실패했습니다.");
        }

        // 홈 페이지로 리다이렉트
        return "redirect:/home/homepage";
    }
}
