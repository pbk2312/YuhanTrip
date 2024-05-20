package hello.yuhanmarket.controller;

import hello.yuhanmarket.dto.LoginDTO;
import hello.yuhanmarket.dto.register.MemberRequestDTO;
import hello.yuhanmarket.dto.token.TokenDTO;
import hello.yuhanmarket.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Log4j2
@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;


    @GetMapping("/register")
    public String showRegisterForm(@ModelAttribute MemberRequestDTO memberRequestDTO) {
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute MemberRequestDTO memberRequestDTO, BindingResult bindingResult) {

        try {
            String result = memberService.register(memberRequestDTO);
            log.info("Registration result: {}", result);
            // 회원 등록이 성공했을 때 어디로 리다이렉트할지에 대한 로직 추가
            return "redirect:/member/login"; // 예시로 로그인 페이지로 리다이렉션
        } catch (Exception e) {
            log.error("Error registering member: {}", e.getMessage());
            // 에러 발생 시 어디로 리다이렉트할지에 대한 로직 추가
            return "redirect:/member/error"; // 예시로 에러 페이지로 리다이렉션
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


}
