package hello.yuhanTrip.controller.restApi;

import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.dto.member.InquiryDTO;
import hello.yuhanTrip.service.member.InquiryService;
import hello.yuhanTrip.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;
    private final MemberService memberService;

    @PostMapping("/submit")
    public ResponseEntity<String> submitInquiry(@RequestBody InquiryDTO inquiryDTO,
                                                @CookieValue(value = "accessToken", required = false) String accessToken) {
        Member member = memberService.getUserDetails(accessToken);
        inquiryService.saveInquiry(inquiryDTO, member);
        return ResponseEntity.ok("문의가 성공적으로 접수되었습니다.");
    }
}
