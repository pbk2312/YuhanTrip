package hello.yuhanTrip.controller.restApi;

import hello.yuhanTrip.dto.email.EmailRequestDTO;
import hello.yuhanTrip.exception.CustomException;
import hello.yuhanTrip.service.member.EmailService;
import hello.yuhanTrip.dto.email.EmailVerificationRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final EmailService emailService;


    // 이메일 보내기
    @PostMapping("/sendCertificationMail")
    public ResponseEntity<String> sendCertificationMail(@RequestBody EmailRequestDTO emailRequestDTO) {
        try {
            String message = emailService.sendCertificationMail(emailRequestDTO);
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (CustomException e) {
            log.error("이메일 전송 실패: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("알 수 없는 오류 발생: {}", e.getMessage());
            return new ResponseEntity<>("이메일 전송 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
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
