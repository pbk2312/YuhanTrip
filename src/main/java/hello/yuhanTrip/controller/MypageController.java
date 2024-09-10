package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.accommodation.Accommodation;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.domain.reservation.Reservation;
import hello.yuhanTrip.domain.accommodation.Room;
import hello.yuhanTrip.domain.admin.RoleChangeRequest;
import hello.yuhanTrip.dto.MypageMemberDTO;
import hello.yuhanTrip.service.Accomodation.AccommodationServiceImpl;
import hello.yuhanTrip.service.Accomodation.ReservationService;
import hello.yuhanTrip.service.member.MemberLikeService;
import hello.yuhanTrip.service.member.MemberService;
import hello.yuhanTrip.service.RoleChangeRequestService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final ReservationService reservationService;
    private final AccommodationServiceImpl accommodationService;
    private final RoleChangeRequestService roleChangeRequestService;
    private final MemberLikeService memberLikeService;

    // 비밀번호 확인
    @GetMapping("/check")
    public String mypageCheck(@CookieValue(value = "accessToken", required = false) String accessToken) {
        Member member = memberService.getUserDetails(accessToken);
        log.info("마이페이지 접근 유저 : {}", member.getName());
        return "mypage/mypageCheck";
    }


    // 비밀번호 확인
    @PostMapping("/checkPassword")
    public ResponseEntity<Void> checkPassword(
            @RequestParam("password") String password,
            @CookieValue(value = "accessToken", required = false) String accessToken,
            HttpSession session
    ) {
        Member member = memberService.getUserDetails(accessToken);
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

        Member member = memberService.getUserDetails(accessToken);

        if (session.getAttribute("passwordChecked") == null) {
            return "redirect:/mypage/check";
        }
        MypageMemberDTO mypageMemberDTO = MypageMemberDTO.builder()
                .email(member.getEmail())
                .address(member.getAddress())
                .nickname(member.getNickname())
                .phoneNumber(member.getPhoneNumber())
                .dateOfBirth(member.getDateOfBirth())
                .name(member.getName())
                .build();
        model.addAttribute("MypageMemberDTO", mypageMemberDTO);
        return "mypage/memberInfo";
    }


    // 개인정보 수정
    @GetMapping("/editMemberInfo")
    public String getEditMemberInfo(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            HttpSession session,
            Model model
    ) {

        Member member = memberService.getUserDetails(accessToken);
        if (session.getAttribute("passwordChecked") == null) {
            return "redirect:/mypage/check";
        }
        MypageMemberDTO mypageMemberDTO = MypageMemberDTO.builder()
                .email(member.getEmail())
                .address(member.getAddress())
                .nickname(member.getNickname())
                .phoneNumber(member.getPhoneNumber())
                .dateOfBirth(member.getDateOfBirth())
                .name(member.getName())
                .build();
        model.addAttribute("MypageMemberDTO", mypageMemberDTO);
        return "mypage/editMemberInfo";
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

        Member member = memberService.getUserDetails(accessToken);

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
        log.info("숙소 조회");
        Member member = memberService.validateHost(accessToken);
        if (session.getAttribute("passwordChecked") == null) {
            return "redirect:/mypage/check";
        }
        List<Accommodation> accommodations = memberService.getAccommodationsByMemberId(member.getId());
        model.addAttribute("accommodations", accommodations);
        return "mypage/accommodationByMember";
    }


    // 예약 상황
    @GetMapping("/reservationSituation")
    public String reservationSituation(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam(value = "accommodationId", required = false) Long accommodationId,
            HttpSession session,
            Model model
    ) {
        Member member = memberService.getUserDetails(accessToken);
        if (session.getAttribute("passwordChecked") == null) {
            return "redirect:/mypage/check";
        }
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
        return "mypage/reservationSituation";
    }


    // 예약 취소
    @PostMapping("/cancelReservation")
    public ResponseEntity<String> cancelReservation(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam("reservationUid") String reservationUid
    ) {

        Member member = memberService.getUserDetails(accessToken);
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
    public String roleChangeRequestForm(@CookieValue(value = "accessToken", required = false) String accessToken, Model model,HttpSession session) {

        Member member = memberService.getUserDetails(accessToken);
        if (session.getAttribute("passwordChecked") == null) {
            return "redirect:/mypage/check";
        }
        model.addAttribute("member", member);
        return "mypage/roleChangeRequestForm";
    }


    // 호스트 승급 신청
    @PostMapping("/roleChangeRequest")
    public String roleChangeRequest(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam("file") MultipartFile file,
            @RequestParam("accommodationTitle") String accommodationTitle,
            @RequestParam("accommodationDescription") String accommodationDescription) {

        Member member = memberService.getUserDetails(accessToken);
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
            @CookieValue(value = "accessToken", required = false) String accessToken,HttpSession session,
            Model model
    ) {
        Member member = memberService.getUserDetails(accessToken);
        if (session.getAttribute("passwordChecked") == null) {
            return "redirect:/mypage/check";
        }
        List<RoleChangeRequest> requestByMember = roleChangeRequestService.getRequestByMember(member);


        model.addAttribute("requestByMember", requestByMember);

        return "mypage/roleChangeRequestList";

    }

    // 좋아요 목록 가져오기
    @GetMapping("/memberLikeHistory")
    public String memberLikeHistory(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "3") int size,
            Model model
    ) {
        Member member = memberService.getUserDetails(accessToken);

        try {
            // 페이지 번호와 사이즈 검증
            page = Math.max(page, 0); // 페이지 번호는 음수일 수 없음
            size = Math.max(size, 1); // 페이지 사이즈는 1 이상이어야 함

            Pageable pageable = PageRequest.of(page, size);
            Page<Accommodation> likesByMember = memberLikeService.getLikesByMember(member, pageable);

            model.addAttribute("likesByMember", likesByMember);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", likesByMember.getTotalPages());
            model.addAttribute("pageSize", size); // 페이지 사이즈 모델에 추가

            return "accommodation/likesByMember";
        } catch (Exception e) {
            log.error("Error processing /memberLikeHistory request", e);
            return "error"; // 오류 페이지로 리다이렉트
        }
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
    }





}
