package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.domain.Reservation;
import hello.yuhanTrip.domain.Room;
import hello.yuhanTrip.domain.admin.RoleChangeRequest;
import hello.yuhanTrip.dto.MypageMemberDTO;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.service.Accomodation.AccommodationServiceImpl;
import hello.yuhanTrip.service.Accomodation.ReservationService;
import hello.yuhanTrip.service.MemberService;
import hello.yuhanTrip.service.RoleChangeRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.Banner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/mypage")
public class MypageController {

    private final TokenProvider tokenProvider;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final ReservationService reservationService;
    private final AccommodationServiceImpl accommodationService;
    private final RoleChangeRequestService roleChangeRequestService;


    // 비밀번호 확인
    @GetMapping("/check")
    public String mypageCheck(@CookieValue(value = "accessToken", required = false) String accessToken) {
        if (validateAccessToken(accessToken) != null) {
            return "redirect:/member/login";
        }
        UserDetails userDetails = getUserDetailsOrRedirect(accessToken);
        if (userDetails == null) {
            return "redirect:/member/login";
        }
        log.info("마이페이지 접근 유저 : {}", userDetails.getUsername());
        return "/mypage/mypageCheck";
    }


    // 비밀번호 확인
    @PostMapping("/checkPassword")
    public ResponseEntity<Void> checkPassword(
            @RequestParam("password") String password,
            @CookieValue(value = "accessToken", required = false) String accessToken,
            HttpSession session
    ) {
        ResponseEntity<Void> validationResponse = validateAccessToken(accessToken);
        if (validationResponse != null) {
            return validationResponse;
        }
        UserDetails userDetails = getUserDetails(accessToken);
        Member member = findMemberByEmail(userDetails.getUsername());
        validatePassword(password, member.getPassword());
        session.setAttribute("passwordChecked", true);
        return ResponseEntity.ok().build();
    }

    // 개인 정보 보기

    @GetMapping("/memberInfo")
    public String memberInfo(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            HttpSession session,
            Model model
    ) {
        if (validateAccessToken(accessToken) != null) {
            return "redirect:/member/login";
        }

        if (validateAccessToken(accessToken) != null || session.getAttribute("passwordChecked") == null) {
            return "redirect:/mypage/check";
        }
        UserDetails userDetails = getUserDetails(accessToken);
        Member member = findMemberByEmail(userDetails.getUsername());
        MypageMemberDTO mypageMemberDTO = MypageMemberDTO.builder()
                .email(member.getEmail())
                .address(member.getAddress())
                .nickname(member.getNickname())
                .phoneNumber(member.getPhoneNumber())
                .dateOfBirth(member.getDateOfBirth())
                .name(member.getName())
                .build();
        model.addAttribute("MypageMemberDTO", mypageMemberDTO);
        return "/mypage/memberInfo";
    }


    // 개인정보 수정
    @GetMapping("/editMemberInfo")
    public String getEditMemberInfo(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            HttpSession session,
            Model model
    ) {
        if (validateAccessToken(accessToken) != null || session.getAttribute("passwordChecked") == null) {
            return "redirect:/mypage/check";
        }
        UserDetails userDetails = getUserDetails(accessToken);
        Member member = findMemberByEmail(userDetails.getUsername());
        MypageMemberDTO mypageMemberDTO = MypageMemberDTO.builder()
                .email(member.getEmail())
                .address(member.getAddress())
                .nickname(member.getNickname())
                .phoneNumber(member.getPhoneNumber())
                .dateOfBirth(member.getDateOfBirth())
                .name(member.getName())
                .build();
        model.addAttribute("MypageMemberDTO", mypageMemberDTO);
        return "/mypage/editMemberInfo";
    }


    // 개인정보 수정
    @PostMapping("/editMemberInfoSubmit")
    public ResponseEntity<Void> editMemberInfoSubmit(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam String name,
            @RequestParam String nickname,
            @RequestParam String phoneNumber,
            @RequestParam LocalDate dateOfBirth,
            @RequestParam String address
    ) {
        ResponseEntity<Void> validationResponse = validateAccessToken(accessToken);
        if (validationResponse != null) {
            return validationResponse;
        }
        UserDetails userDetails = getUserDetails(accessToken);
        Member member = findMemberByEmail(userDetails.getUsername());

        member.setName(name);
        member.setNickname(nickname);
        member.setPhoneNumber(phoneNumber);
        member.setDateOfBirth(dateOfBirth);
        member.setAddress(address);

        try {
            memberService.updateMember(member);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("회원 정보 수정 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 숙소 등록 리스트

    @GetMapping("/memberAccommodations")
    public String getAccommodationsByMembers(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            HttpSession session,
            Model model
    ) {
        if (validateAccessToken(accessToken) != null || session.getAttribute("passwordChecked") == null) {
            return "redirect:/mypage/check";
        }
        UserDetails userDetails = getUserDetails(accessToken);
        Member member = findMemberByEmail(userDetails.getUsername());
        List<Accommodation> accommodations = memberService.getAccommodationsByMemberId(member.getId());
        model.addAttribute("accommodations", accommodations);
        return "/mypage/accommodationByMember";
    }


    // 예약 상황
    @GetMapping("/reservationSituation")
    public String reservationSituation(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam(value = "accommodationId", required = false) Long accommodationId,
            HttpSession session,
            Model model
    ) {
        if (validateAccessToken(accessToken) != null || session.getAttribute("passwordChecked") == null) {
            return "redirect:/mypage/check";
        }
        UserDetails userDetails = getUserDetails(accessToken);
        Member member = findMemberByEmail(userDetails.getUsername());

        if (accommodationId == null) {
            return "redirect:/mypage/memberAccommodations";
        }

        Accommodation accommodation = accommodationService.getAccommodationInfo(accommodationId);
        if (!member.equals(accommodation.getMember())) {
            return "redirect:/mypage/memberAccommodations";
        }

        Map<Room, List<Reservation>> roomReservationsMap = new HashMap<>();
        for (Room room : accommodation.getRooms()) {
            List<Reservation> reservations = reservationService.getReservationsByRoomId(room.getId());
            roomReservationsMap.put(room, reservations);
        }

        model.addAttribute("accommodation", accommodation);
        model.addAttribute("roomReservationsMap", roomReservationsMap);
        return "/mypage/reservationSituation";
    }


    // 예약 취소
    @PostMapping("/cancelReservation")
    public ResponseEntity<String> cancelReservation(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam("reservationUid") String reservationUid
    ) {
        if (validateAccessToken(accessToken) != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인이 필요합니다.");
        }

        UserDetails userDetails = getUserDetails(accessToken);
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("유효하지 않은 사용자입니다.");
        }

        try {
            log.info("예약 거절 요청 - reservationUid: {}", reservationUid);
            boolean isCancelled = reservationService.cancelReservation(reservationUid);
            return isCancelled
                    ? ResponseEntity.ok("예약이 성공적으로 취소되었습니다.")
                    : ResponseEntity.status(HttpStatus.NOT_FOUND).body("예약을 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("예약 취소 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("예약 취소 중 오류가 발생했습니다.");
        }
    }


    // 호스트 승급 신청
    @GetMapping("/roleChangeRequestForm")
    public String roleChangeRequestForm(@CookieValue(value = "accessToken", required = false) String accessToken, Model model) {
        if (validateAccessToken(accessToken) != null) {
            return "redirect:/login";
        }

        UserDetails userDetails = getUserDetails(accessToken);
        if (userDetails == null) {
            return "redirect:/login";
        }

        Member member = findMemberByEmail(userDetails.getUsername());
        if (member == null) {
            return "redirect:/error";
        }

        model.addAttribute("member", member);
        return "/mypage/roleChangeRequestForm";
    }


    // 호스트 승급 신청
    @PostMapping("/roleChangeRequest")
    public String roleChangeRequest(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam("file") MultipartFile file,
            @RequestParam("accommodationTitle") String accommodationTitle,
            @RequestParam("accommodationDescription") String accommodationDescription) {
        if (validateAccessToken(accessToken) != null) {
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        }

        UserDetails userDetails = getUserDetails(accessToken);
        if (userDetails == null) {
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        }

        Member member = findMemberByEmail(userDetails.getUsername());
        if (member == null) {
            return "redirect:/error"; // 오류 페이지로 리다이렉트
        }

        try {
            roleChangeRequestService.requestRoleChange(member, file, accommodationTitle, accommodationDescription);
            return "redirect:/mypage/roleChangeRequestList"; // 성공 시 리스트 페이지로 리다이렉트
        } catch (IllegalStateException e) {
            return "redirect:/error?message=" + e.getMessage(); // 오류 페이지로 리다이렉트
        } catch (Exception e) {
            log.error("역할 변경 요청 처리 중 오류 발생: ", e);
            return "redirect:/error?message=역할 변경 요청 처리 중 오류가 발생했습니다."; // 오류 페이지로 리다이렉트
        }
    }


    // 호스트 승급 신청 리스트
    @GetMapping("/roleChangeRequestList")
    public String roleChangeRequestList(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            Model model
    ) {
        if (validateAccessToken(accessToken) != null) {
            return "redirect:/login";
        }

        UserDetails userDetails = getUserDetails(accessToken);
        if (userDetails == null) {
            return "redirect:/login";
        }

        Member member = findMemberByEmail(userDetails.getUsername());
        if (member == null) {
            return "redirect:/error";
        }


        List<RoleChangeRequest> requestByMember = roleChangeRequestService.getRequestByMember(member);


        model.addAttribute("requestByMember", requestByMember);

        return "/mypage/roleChangeRequestList";

    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
    }

    // 토큰 유효성 검사 및 사용자 세부정보 가져오기
    private UserDetails getUserDetailsOrRedirect(String accessToken) {
        if (isInvalidToken(accessToken)) {
            return null; // 토큰이 유효하지 않으면 null 반환
        }
        return getUserDetails(accessToken);
    }

    private boolean isInvalidToken(String accessToken) {
        return accessToken == null || !tokenProvider.validate(accessToken);
    }

    private UserDetails getUserDetails(String accessToken) {
        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        return (UserDetails) authentication.getPrincipal();
    }

    private Member findMemberByEmail(String email) {
        return memberService.findByEmail(email);
    }

    private ResponseEntity<Void> validateAccessToken(String accessToken) {
        if (isInvalidToken(accessToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return null;
    }
}
