package hello.yuhanTrip.controller.restApi;

import hello.yuhanTrip.dto.ResponseDTO;
import hello.yuhanTrip.dto.coupon.Coupon;
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
    public ResponseEntity<ResponseDTO<?>> sendCertificationMail(@RequestBody EmailRequestDTO emailRequestDTO) {
        try {
            // 이메일 전송 서비스 호출
            String message = emailService.sendCertificationMail(emailRequestDTO);
            ResponseDTO<String> response = new ResponseDTO<>(message, null);
            return ResponseEntity.ok(response);

        } catch (CustomException e) {
            // 사용자 정의 예외 처리
            log.error("이메일 전송 실패: {}", e.getMessage());
            ResponseDTO<String> response = new ResponseDTO<>(e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            // 일반적인 예외 처리
            log.error("알 수 없는 오류 발생: {}", e.getMessage());
            ResponseDTO<String> response = new ResponseDTO<>("이메일 전송 중 오류가 발생했습니다.", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    // 이메일 확인
    @PostMapping("/verifyEmail")
    public ResponseEntity<ResponseDTO<?>> verifyEmail(@RequestBody EmailVerificationRequestDTO emailVerificationRequestDTO) {
        try {
            emailService.verifyEmail(emailVerificationRequestDTO.getEmail(), emailVerificationRequestDTO.getCertificationNumber());
            ResponseDTO<String> response = new ResponseDTO<>("인증번호 인증이 완료 되었습니다.", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("이메일 인증 실패: {}", e.getMessage());
            ResponseDTO<String> response = new ResponseDTO<>("인증번호 인증 실패", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


}
