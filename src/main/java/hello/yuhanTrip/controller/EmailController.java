package hello.yuhanTrip.controller;

import hello.yuhanTrip.dto.email.EmailRequestDTO;
import hello.yuhanTrip.service.EmailService;
import hello.yuhanTrip.dto.email.EmailVerificationRequestDTO;
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


    // 이메일 보내기
    @PostMapping("/sendCertificationMail")
    public ResponseEntity<String> sendCertificationMail(@RequestBody EmailRequestDTO emailRequestDTO) {
        try {
            emailService.sendCertificationMail(emailRequestDTO);
            return new ResponseEntity<>("이메일 전송이 완료되었습니다.", HttpStatus.OK);
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", e.getMessage());
            return new ResponseEntity<>("이메일 전송에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // 이메일 확인
    @PostMapping("/verifyEmail")
    public ResponseEntity<String> verifyEmail(@RequestBody EmailVerificationRequestDTO emailVerificationRequestDTO) {
        try {
            emailService.verifyEmail(emailVerificationRequestDTO.getEmail(), emailVerificationRequestDTO.getCertificationNumber());
            return new ResponseEntity<>("인증번호 인증이 완료되었습니다.", HttpStatus.OK);
        } catch (Exception e) {
            log.error("이메일 인증 실패: {}", e.getMessage());
            return new ResponseEntity<>("이메일 인증에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
