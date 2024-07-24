package hello.yuhanTrip.controller;


import hello.yuhanTrip.dto.WithdrawalMembershipDTO;
import hello.yuhanTrip.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageController {

    private final MemberService memberService;


    @GetMapping("/withdrawalMembership")
    public String withdrawalMembershipForm(Model model,
                                           @AuthenticationPrincipal UserDetails userDetails
    )

    {
        log.info("로그인된 사용자 : {}" ,userDetails.getUsername());
        WithdrawalMembershipDTO withdrawalMembershipDTO = new WithdrawalMembershipDTO();
        model.addAttribute("withdrawalMembershipDTO", withdrawalMembershipDTO);

        return "withdrawalMembership";
    }




    @PostMapping("/deleteAccount")
    public ResponseEntity<Void> deleteAccount(@RequestBody WithdrawalMembershipDTO withdrawalMembershipDTO,
                                              @AuthenticationPrincipal UserDetails userDetails) {

        log.info("회원 탈퇴를 진행합니다...");

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
