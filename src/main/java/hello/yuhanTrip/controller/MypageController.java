package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.domain.Reservation;
import hello.yuhanTrip.domain.Room;
import hello.yuhanTrip.dto.AccommodationRegisterDTO;
import hello.yuhanTrip.dto.MypageMemberDTO;
import hello.yuhanTrip.dto.RoomDTO;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
import hello.yuhanTrip.service.Accomodation.ReservationService;
import hello.yuhanTrip.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/mypage")
public class MypageController {

    private final TokenProvider tokenProvider;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final ReservationService reservationService;
    private final AccommodationService accommodationService;

    @GetMapping("/check")
    public String mypageCheck(
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {
        if (isInvalidToken(accessToken)) {
            return "redirect:/member/login";
        }

        UserDetails userDetails = getUserDetails(accessToken);
        log.info("마이페이지 접근 유저 : {} ", userDetails.getUsername());

        return "/mypage/mypageCheck";
    }

    @PostMapping("/checkPassword")
    public ResponseEntity<Void> checkPassword(
            @RequestParam("password") String password,
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {

        ResponseEntity<Void> validationResponse = validateAccessToken(accessToken);
        if (validationResponse != null) {
            return validationResponse;
        }

        UserDetails userDetails = getUserDetails(accessToken);
        Member member = findMemberByEmail(userDetails.getUsername());

        validatePassword(password, member.getPassword());

        return ResponseEntity.ok().build(); // 비밀번호가 일치하면 OK 응답 반환
    }

    @GetMapping("/memberInfo")
    public String memberInfo(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            Model model
    ) {
        ResponseEntity<Void> validationResponse = validateAccessToken(accessToken);
        if (validationResponse != null) {
            return "redirect:/member/login";
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


    @GetMapping("/editMemberInfo")
    public String geteditMemberInfo(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            Model model
    ) {

        ResponseEntity<Void> validationResponse = validateAccessToken(accessToken);
        if (validationResponse != null) {
            return "redirect:/member/login";
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


    @PostMapping("/editMemberInfoSubmit")
    public ResponseEntity<Void> editMemberInfoSubmit(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam String name,
            @RequestParam String nickname,
            @RequestParam String phoneNumber,
            @RequestParam LocalDate dateOfBirth,
            @RequestParam String address
    ) {
        // 1. 토큰 유효성 검사
        ResponseEntity<Void> validationResponse = validateAccessToken(accessToken);
        if (validationResponse != null) {
            return validationResponse;
        }

        // 2. 사용자 정보 가져오기
        UserDetails userDetails = getUserDetails(accessToken);
        Member member = findMemberByEmail(userDetails.getUsername());

        // 3. 회원 정보 업데이트
        member.setName(name);
        member.setNickname(nickname);
        member.setPhoneNumber(phoneNumber);
        member.setDateOfBirth(dateOfBirth);
        member.setAddress(address);

        try {
            // 4. 업데이트된 정보 저장
            memberService.updateMember(member);

            // 5. 성공적으로 업데이트된 경우 OK 응답 반환
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("회원 정보 수정 중 오류 발생: ", e);
            // 6. 오류 발생 시 INTERNAL_SERVER_ERROR 응답 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/memberAccommodations")
    public String getAccommodationsByMembers(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            Model model
    ) {
        ResponseEntity<Void> validationResponse = validateAccessToken(accessToken);
        if (validationResponse != null) {
            return "redirect:/member/login";
        }


        UserDetails userDetails = getUserDetails(accessToken);
        Member member = findMemberByEmail(userDetails.getUsername());

        List<Accommodation> accommodations = memberService.getAccommodationsByMemberId(member.getId());
        model.addAttribute("accommodations", accommodations);

        return "/mypage/accommodationByMember";
    }

    @GetMapping("/reservationSituation")
    public String reservationAccommodationByMember(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            Model model
    ){
        ResponseEntity<Void> validationResponse = validateAccessToken(accessToken);
        if (validationResponse != null) {
            return "redirect:/member/login";
        }


        UserDetails userDetails = getUserDetails(accessToken);
        Member member = findMemberByEmail(userDetails.getUsername());


        // 1. 멤버가 등록한 숙소 목록을 가져옴
        List<Accommodation> accommodations = memberService.getAccommodationsByMemberId(member.getId());

        // 2. 숙소별 룸과 해당 룸의 예약 정보를 조회
        Map<Accommodation, Map<Room, List<Reservation>>> reservationInfo = new HashMap<>();

        for (Accommodation accommodation : accommodations) {
            Map<Room, List<Reservation>> roomReservations = new HashMap<>();
            for (Room room : accommodation.getRooms()) {
                List<Reservation> reservations = reservationService.getReservationsByRoomId(room.getId());
                roomReservations.put(room, reservations);
            }
            reservationInfo.put(accommodation, roomReservations);
        }

        // 3. 뷰에 사용할 모델에 데이터를 추가
        model.addAttribute("reservationInfo", reservationInfo);

        return "/mypage/reservationSituation"; // reservationSituation.html로 이동




    }





    // 인증 토큰 유효성 검증 메소드
    private ResponseEntity<Void> validateAccessToken(String accessToken) {
        if (isInvalidToken(accessToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return null;
    }

    // 인증 토큰 유효성 검사
    private boolean isInvalidToken(String accessToken) {
        return accessToken == null || !tokenProvider.validate(accessToken);
    }

    // 사용자 세부정보 가져오기
    private UserDetails getUserDetails(String accessToken) {
        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        return (UserDetails) authentication.getPrincipal();
    }

    // 회원 정보 조회
    private Member findMemberByEmail(String email) {
        return memberService.findByEmail(email);
    }

    // 비밀번호 검증
    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
    }
}
