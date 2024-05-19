package hello.yuhanmarket.controller;

import hello.yuhanmarket.dto.EmailRequestDTO;
import hello.yuhanmarket.dto.EmailVerificationRequestDTO;
import hello.yuhanmarket.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/sendCertificationMail")
    public ResponseEntity<String> sendCertificationMail(@RequestBody EmailRequestDTO emailRequestDTO) {
        try {
            String result = emailService.sendCertificationMail(emailRequestDTO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이메일 전송에 실패했습니다.");
        }
    }

    @PostMapping("/verifyEmail")
    public ResponseEntity<String> verifyEmail(@RequestBody EmailVerificationRequestDTO emailVerificationRequestDTO) {
        try {
            String result = emailService.verifyEmail(emailVerificationRequestDTO.getEmail(), emailVerificationRequestDTO.getCertificationNumber());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("이메일 인증 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이메일 인증에 실패했습니다.");
        }
    }
}
