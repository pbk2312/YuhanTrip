package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.*;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
import hello.yuhanTrip.service.MemberLikeService;
import hello.yuhanTrip.service.MemberService;
import hello.yuhanTrip.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/accommodation")
@Log4j2
public class AccommodationController {

    private final AccommodationService accommodationService;
    private final TokenProvider tokenProvider;
    private final MemberService memberService;
    private final MemberLikeService memberLikeService;
    private final ReviewService reviewService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @GetMapping("/accommodations")
    public String listAccommodations(Model model,
                                     @RequestParam(required = false) String region,
                                     @RequestParam(value = "checkin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkin,
                                     @RequestParam(value = "checkout", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkout,
                                     @RequestParam(value = "numGuests", required = false) Integer numGuests,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "12") int size) {

        log.info("숙소 리스트를 조회합니다.... 페이지: {}, 사이즈: {}, 지역: {}, 체크인 날짜: {}, 체크아웃 날짜: {}, 숙박 인원 수: {}", page, size, region, checkin, checkout, numGuests);

        page = Math.max(page, 0);

        Page<Accommodation> accommodationsPage;

        if (region != null && !region.isEmpty()) {
            Integer areaCode = RegionCode.getCodeByRegion(region);
            if (areaCode == null) {
                log.error("잘못된 지역 이름: {}", region);
                return "error";
            }

            if (checkin != null && checkout != null && numGuests != null) {
                accommodationsPage = accommodationService.getAvailableAccommodations(
                        String.valueOf(areaCode), checkin, checkout, numGuests, page, size);
            } else {
                accommodationsPage = accommodationService.getAccommodationsByAreaCode(String.valueOf(areaCode), page, size);
            }

            model.addAttribute("region", region);
        } else {
            if (checkin != null && checkout != null && numGuests != null) {
                accommodationsPage = accommodationService.getAvailableAccommodations(
                        null, checkin, checkout, numGuests, page, size);
            } else {
                accommodationsPage = accommodationService.getAccommodations(page, size);
            }
        }

        // 평균 평점 계산
        List<Accommodation> accommodations = accommodationsPage.getContent();
        for (Accommodation accommodation : accommodations) {
            double averageRating = accommodationService.calculateAverageRating(accommodation.getId());
            accommodationService.updateAverageRating(accommodation.getId(),averageRating);
        }

        int totalPages = accommodationsPage.getTotalPages();
        int currentPage = page;

        int startPage = Math.max(0, currentPage - 5);
        int endPage = Math.min(totalPages - 1, currentPage + 5);

        model.addAttribute("accommodations", accommodations);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("pageSize", size);

        model.addAttribute("checkin", checkin);
        model.addAttribute("checkout", checkout);
        model.addAttribute("numGuests", numGuests);

        log.info("현재 페이지: {}, 전체 페이지: {}, 시작 페이지: {}, 끝 페이지: {}", currentPage, totalPages, startPage, endPage);

        return "/accommodation/accommodations";
    }


    @GetMapping("/byregion")
    public String listAccommodationsByRegion(Model model,
                                             @RequestParam(value = "region", required = false) String region,
                                             @RequestParam(value = "checkin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkin,
                                             @RequestParam(value = "checkout", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkout,
                                             @RequestParam(value = "numGuests", required = false) Integer numGuests,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "12") int size) {

        log.info("지역 코드로 숙소 리스트를 조회합니다. 지역: {}, 페이지: {}, 사이즈: {}, 체크인 날짜: {}, 체크아웃 날짜: {}, 숙박 인원 수: {}", region, page, size, checkin, checkout, numGuests);

        // 페이지 번호와 사이즈 검증
        page = Math.max(page, 0);
        size = Math.max(size, 1);

        Page<Accommodation> accommodationsPage;

        if (region != null && !region.isEmpty()) {
            // 지역 코드가 제공된 경우
            Integer areaCode = RegionCode.getCodeByRegion(region);
            if (areaCode == null) {
                log.error("잘못된 지역 이름: {}", region);
                return "error"; // 잘못된 지역 이름 처리
            }

            if (checkin != null && checkout != null && numGuests != null) {
                // 체크인, 체크아웃, 숙박 인원 수가 모두 제공된 경우
                accommodationsPage = accommodationService.getAvailableAccommodations(
                        String.valueOf(areaCode), checkin, checkout, numGuests, page, size);
            } else {
                // 필터링 없이 전체 숙소 조회
                accommodationsPage = accommodationService.getAccommodationsByAreaCode(
                        String.valueOf(areaCode), page, size);
            }

            model.addAttribute("region", region);
        } else {
            // 지역 코드가 제공되지 않은 경우
            if (checkin != null && checkout != null && numGuests != null) {
                // 체크인, 체크아웃, 숙박 인원 수가 모두 제공된 경우
                accommodationsPage = accommodationService.getAvailableAccommodations(
                        null, checkin, checkout, numGuests, page, size);
            } else {
                // 필터링 없이 전체 숙소 조회
                accommodationsPage = accommodationService.getAccommodations(page, size);
            }
        }

        int totalPages = accommodationsPage.getTotalPages();
        int currentPage = page;

        // 페이지 번호 범위 계산
        int startPage = Math.max(0, currentPage - 5);
        int endPage = Math.min(totalPages - 1, currentPage + 5);

        // 날짜를 포맷팅하여 문자열로 변환
        String checkinFormatted = checkin != null ? checkin.format(formatter) : null;
        String checkoutFormatted = checkout != null ? checkout.format(formatter) : null;

        model.addAttribute("accommodations", accommodationsPage.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("pageSize", size);
        model.addAttribute("region", region);
        model.addAttribute("checkin", checkinFormatted);
        model.addAttribute("checkout", checkoutFormatted);
        model.addAttribute("numGuests", numGuests);

        log.info("현재 페이지: {}, 전체 페이지: {}, 시작 페이지: {}, 끝 페이지: {}", currentPage, totalPages, startPage, endPage);

        return "/accommodation/accommodations"; // 뷰 이름
    }

    @GetMapping("/info")
    public String getAccommodationInfo(
            @RequestParam("id") Long id,
            @RequestParam(value = "checkin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkInDate,
            @RequestParam(value = "checkout", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOutDate,
            @RequestParam(value = "numGuests", required = false) Integer numberOfGuests,
            Model model) {

        log.info("숙소 정보를 가져옵니다... ID: {}", id);
        log.info("선택 사항 - 체크인 날짜: {}, 체크아웃 날짜: {}, 숙박 인원 수: {}", checkInDate, checkOutDate, numberOfGuests);

        Accommodation accommodation = accommodationService.getAccommodationInfo(id);

        List<Room> availableRooms = (checkInDate == null || checkOutDate == null) ?
                accommodation.getRooms() :
                accommodationService.getAvailableRoomsByAccommodation(id, checkInDate, checkOutDate);

        List<Review> reviews = reviewService.getReviewsByAccommodation(id);

        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("checkin", checkInDate);
        model.addAttribute("checkout", checkOutDate);
        model.addAttribute("numGuests", numberOfGuests);
        model.addAttribute("accommodation", accommodation);
        model.addAttribute("reviews", reviews);

        return "/accommodation/accommodationInfo";
    }



    @GetMapping("/memberLikeHistory")
    public String memberLikeHistory(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "3") int size,
            Model model
    ) {
        // 인증 확인
        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            return "redirect:/member/login"; // 로그인 페이지로 리다이렉트
        }

        try {
            // 페이지 번호와 사이즈 검증
            page = Math.max(page, 0); // 페이지 번호는 음수일 수 없음
            size = Math.max(size, 1); // 페이지 사이즈는 1 이상이어야 함

            Authentication authentication = tokenProvider.getAuthentication(accessToken);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Member member = memberService.findByEmail(userDetails.getUsername());

            Pageable pageable = PageRequest.of(page, size);
            Page<Accommodation> likesByMember = memberLikeService.getLikesByMember(member, pageable);

            model.addAttribute("likesByMember", likesByMember);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", likesByMember.getTotalPages());
            model.addAttribute("pageSize", size); // 페이지 사이즈 모델에 추가

            return "/accommodation/likesByMember";
        } catch (Exception e) {
            log.error("Error processing /memberLikeHistory request", e);
            return "error"; // 오류 페이지로 리다이렉트
        }
    }


}