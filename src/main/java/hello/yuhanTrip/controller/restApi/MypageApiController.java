package hello.yuhanTrip.controller.restApi;

import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.dto.ResponseDTO;
import hello.yuhanTrip.dto.payment.MypageMemberDTO;
import hello.yuhanTrip.exception.CustomException;
import hello.yuhanTrip.service.reservation.ReservationService;
import hello.yuhanTrip.service.member.RoleChangeRequestService;
import hello.yuhanTrip.service.member.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;


@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/mypage")
public class MypageApiController {

    private final MemberService memberService;
    private final ReservationService reservationService;
    private final PasswordEncoder passwordEncoder;
    private final RoleChangeRequestService roleChangeRequestService;

    // 비밀번호 확인
    @PostMapping("/checkPassword")
    public ResponseEntity<ResponseDTO<Void>> checkPassword(
            @RequestParam("password") String password,
            @CookieValue(value = "accessToken", required = false) String accessToken,
            HttpSession session
    ) {
        Member member = memberService.getUserDetails(accessToken);
        validatePassword(password, member.getPassword());
        session.setAttribute("passwordChecked", true);
        return ResponseEntity.ok(new ResponseDTO<>("비밀번호 확인이 완료되었습니다.", null));
    }


    // 개인정보 수정
    @PostMapping("/editMemberInfoSubmit")
    public ResponseEntity<ResponseDTO<Void>> editMemberInfoSubmit(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam("email") String email,
            @RequestParam("name") String name,
            @RequestParam("nickname") String nickname,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("dateOfBirth") String dateOfBirth,
            @RequestParam("address") String address
    ) {
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDTO<>("로그인이 필요합니다.", null));
        }

        try {
            Member member = memberService.getUserDetails(accessToken);
            if (member == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO<>("회원 정보를 찾을 수 없습니다.", null));
            }
            LocalDate birth = LocalDate.parse(dateOfBirth); // String을 LocalDate로 변환
            MypageMemberDTO mypageMemberDTO = new MypageMemberDTO(email, name, nickname, phoneNumber, address, birth);
            memberService.updateMember(member, mypageMemberDTO);

            return ResponseEntity.ok(new ResponseDTO<>("회원 정보가 성공적으로 수정되었습니다.", null));
        } catch (Exception e) {
            log.error("회원 정보 수정 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDTO<>("회원 정보 수정 중 오류가 발생했습니다.", null));
        }
    }



    // 예약 취소
    @PostMapping("/cancelReservation")
    public ResponseEntity<ResponseDTO<Void>> cancelReservation(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam("reservationUid") String reservationUid
    ) {
        Member member = memberService.getUserDetails(accessToken);
        try {
            log.info("예약 거절 요청 - reservationUid: {}", reservationUid);
            boolean isCancelled = reservationService.cancelReservation(reservationUid);
            return isCancelled
                    ? ResponseEntity.ok(new ResponseDTO<>("예약이 성공적으로 취소되었습니다.", null))
                    : ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO<>("예약을 찾을 수 없습니다.", null));
        } catch (Exception e) {
            log.error("예약 취소 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>("예약 취소 중 오류가 발생했습니다.", null));
        }
    }

    // 호스트 승급 신청
    @PostMapping("/roleChangeRequest")
    public ResponseEntity<String> roleChangeRequest(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam("file") MultipartFile file,
            @RequestParam("accommodationTitle") String accommodationTitle,
            @RequestParam("accommodationDescription") String accommodationDescription) {

        Member member = memberService.getUserDetails(accessToken);
        try {
            // 역할 변경 요청 처리
            roleChangeRequestService.requestRoleChange(member, file, accommodationTitle, accommodationDescription);

            // 성공 시 200 OK와 성공 메시지 반환
            return ResponseEntity.ok("역할 변경 요청이 성공적으로 처리되었습니다.");
        } catch (IllegalStateException e) {
            // IllegalStateException 발생 시 400 Bad Request 상태와 에러 메시지 반환
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("역할 변경 요청 실패: " + e.getMessage());
        } catch (Exception e) {
            // 기타 예외 발생 시 500 Internal Server Error 상태와 에러 메시지 반환
            log.error("역할 변경 요청 처리 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("역할 변경 요청 처리 중 오류가 발생했습니다.");
        }
    }


    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new CustomException("비밀번호가 일치하지 않습니다.");
        }
    }
}
