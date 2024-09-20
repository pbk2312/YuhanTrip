package hello.yuhanTrip.controller.view;

import hello.yuhanTrip.domain.accommodation.Accommodation;
import hello.yuhanTrip.domain.member.AuthProvider;
import hello.yuhanTrip.domain.member.Inquiry;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.domain.reservation.Reservation;
import hello.yuhanTrip.domain.accommodation.Room;
import hello.yuhanTrip.domain.admin.RoleChangeRequest;
import hello.yuhanTrip.dto.accommodation.AccommodationDTO;
import hello.yuhanTrip.dto.member.CouponDTO;
import hello.yuhanTrip.dto.member.MemberDTO;
import hello.yuhanTrip.mapper.AccommodationMapper;
import hello.yuhanTrip.mapper.MemberMapper;
import hello.yuhanTrip.service.Accomodation.AccommodationServiceImpl;
import hello.yuhanTrip.service.discount.CouponService;
import hello.yuhanTrip.service.reservation.ReservationService;
import hello.yuhanTrip.service.member.MemberLikeService;
import hello.yuhanTrip.service.member.MemberService;
import hello.yuhanTrip.service.member.RoleChangeRequestService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/mypage")
public class MypageViewController {

    private final MemberService memberService;
    private final ReservationService reservationService;
    private final AccommodationServiceImpl accommodationService;
    private final RoleChangeRequestService roleChangeRequestService;
    private final MemberLikeService memberLikeService;
    private final CouponService couponService;

    // 비밀번호 확인
    @GetMapping("/check")
    public String mypageCheck(@CookieValue(value = "accessToken", required = false) String accessToken) {
        Member member = memberService.getUserDetails(accessToken);

        // AuthProvider가 KAKAO인 경우 바로 /memberInfo로 리다이렉트
        if (member.getAuthProvider() == AuthProvider.KAKAO) {
            return "redirect:/mypage/memberInfo";
        }

        log.info("마이페이지 접근 유저 : {}", member.getName());
        return "mypage/mypageCheck";
    }


    // 개인 정보 보기
    @GetMapping("/memberInfo")
    public String memberInfo(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            HttpSession session,
            Model model
    ) {
        // Member 엔티티를 가져옵니다.
        Member member = memberService.getUserDetails(accessToken);

        // AuthProvider가 KAKAO인 경우 비밀번호 확인 생략
        if (member.getAuthProvider() != AuthProvider.KAKAO) {
            // 세션에서 passwordChecked 속성을 확인합니다.
            if (session.getAttribute("passwordChecked") == null) {
                return "redirect:/mypage/check";
            }
        }

        // Member 엔티티를 MemberDTO로 변환합니다.
        MemberDTO memberDTO = MemberMapper.INSTANCE.toMemberDTO(member);

        // 모델에 MemberDTO를 추가합니다.
        model.addAttribute("MypageMemberDTO", memberDTO);

        // 뷰를 반환합니다.
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

        // AuthProvider가 KAKAO인 경우 비밀번호 확인 생략
        if (member.getAuthProvider() != AuthProvider.KAKAO) {
            // 세션에서 passwordChecked 속성을 확인합니다.
            if (session.getAttribute("passwordChecked") == null) {
                return "redirect:/mypage/check";
            }
        }

        // Member 엔티티를 MemberDTO로 변환합니다.
        MemberDTO memberDTO = MemberMapper.INSTANCE.toMemberDTO(member);

        model.addAttribute("MypageMemberDTO", memberDTO);
        return "mypage/editMemberInfo";
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
        // AuthProvider가 KAKAO인 경우 비밀번호 확인 생략
        if (member.getAuthProvider() != AuthProvider.KAKAO) {
            // 세션에서 passwordChecked 속성을 확인합니다.
            if (session.getAttribute("passwordChecked") == null) {
                return "redirect:/mypage/check";
            }
        }
        List<Accommodation> accommodations = memberService.getAccommodationsByMemberId(member.getId());
        List<AccommodationDTO> dtoList = AccommodationMapper.INSTANCE.toDTOList(accommodations);
        model.addAttribute("accommodations", dtoList);
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
        // AuthProvider가 KAKAO인 경우 비밀번호 확인 생략
        if (member.getAuthProvider() != AuthProvider.KAKAO) {
            // 세션에서 passwordChecked 속성을 확인합니다.
            if (session.getAttribute("passwordChecked") == null) {
                return "redirect:/mypage/check";
            }
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

        AccommodationDTO dto = AccommodationMapper.INSTANCE.toDTO(accommodation);
        model.addAttribute("accommodation", dto);
        model.addAttribute("roomReservationsMap", roomReservationsMap);
        return "mypage/reservationSituation";
    }


    // 호스트 승급 신청
    @GetMapping("/roleChangeRequestForm")
    public String roleChangeRequestForm(@CookieValue(value = "accessToken", required = false) String accessToken, Model model, HttpSession session) {

        Member member = memberService.getUserDetails(accessToken);
        // AuthProvider가 KAKAO인 경우 비밀번호 확인 생략
        if (member.getAuthProvider() != AuthProvider.KAKAO) {
            // 세션에서 passwordChecked 속성을 확인합니다.
            if (session.getAttribute("passwordChecked") == null) {
                return "redirect:/mypage/check";
            }
        }
        MemberDTO memberDTO = MemberMapper.INSTANCE.toMemberDTO(member);
        model.addAttribute("member", memberDTO);
        return "mypage/roleChangeRequestForm";
    }


    // 호스트 승급 신청 리스트
    @GetMapping("/roleChangeRequestList")
    public String roleChangeRequestList(
            @CookieValue(value = "accessToken", required = false) String accessToken, HttpSession session,
            Model model
    ) {
        Member member = memberService.getUserDetails(accessToken);
        // AuthProvider가 KAKAO인 경우 비밀번호 확인 생략
        if (member.getAuthProvider() != AuthProvider.KAKAO) {
            // 세션에서 passwordChecked 속성을 확인합니다.
            if (session.getAttribute("passwordChecked") == null) {
                return "redirect:/mypage/check";
            }
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

    @GetMapping("couponList")
    public String couponList(@CookieValue(value = "accessToken", required = false) String accessToken,
                             Model model
    ) {
        Member member = memberService.getUserDetails(accessToken);

        List<CouponDTO> listCoupon = couponService.getListCoupon(member);

        model.addAttribute("listCoupon", listCoupon);

        return "mypage/listCoupon";
    }

    @GetMapping("inquiriesList")
    public String inquiriesList(@CookieValue(value = "accessToken", required = false) String accessToken,
                                Model model) {
        Member member = memberService.getUserDetails(accessToken);

        List<Inquiry> inquiries = member.getInquiries();
        model.addAttribute("inquiries", inquiries);

        return "mypage/inquiriesList";
    }


}
