package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.*;
import hello.yuhanTrip.dto.AccommodationRegisterDTO;
import hello.yuhanTrip.dto.RoomDTO;
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
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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



    @GetMapping("/byregion")
    public String listAccommodationsByRegion(Model model,
                                             @RequestParam(value = "region", required = false) String region,
                                             @RequestParam(value = "checkin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkin,
                                             @RequestParam(value = "checkout", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkout,
                                             @RequestParam(value = "numGuests", required = false) Integer numGuests,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "12") int size,
                                             @RequestParam(required = false) String sort) {  // sort 파라미터 추가

        log.info("지역 코드로 숙소 리스트를 조회합니다. 지역: {}, 페이지: {}, 사이즈: {}, 체크인 날짜: {}, 체크아웃 날짜: {}, 숙박 인원 수: {}, 정렬: {}", region, page, size, checkin, checkout, numGuests, sort);

        // 페이지 번호와 사이즈 검증
        page = Math.max(page, 0);
        size = Math.max(size, 1);

        Page<Accommodation> accommodationsPage;
        boolean filterByAvailability = (checkin != null && checkout != null && numGuests != null);

        if (region != null && !region.isEmpty()) {
            // 지역 코드가 제공된 경우
            Integer areaCode = RegionCode.getCodeByRegion(region);
            if (areaCode == null) {
                log.error("잘못된 지역 이름: {}", region);
                return "error"; // 잘못된 지역 이름 처리
            }

            accommodationsPage = fetchAccommodationsWithSortingAndFiltering(
                    areaCode, filterByAvailability, checkin, checkout, numGuests, page, size, sort);

            model.addAttribute("region", region);
        } else {
            // 지역 코드가 제공되지 않은 경우
            accommodationsPage = fetchAccommodationsWithSortingAndFiltering(
                    null, filterByAvailability, checkin, checkout, numGuests, page, size, sort);
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
        model.addAttribute("sort", sort);
        model.addAttribute("searchType", "region");

        log.info("현재 페이지: {}, 전체 페이지: {}, 시작 페이지: {}, 끝 페이지: {}", currentPage, totalPages, startPage, endPage);

        return "/accommodation/accommodations"; // 뷰 이름
    }


    private Page<Accommodation> fetchAccommodationsWithSortingAndFiltering(Integer areaCode,
                                                                           boolean filterByAvailability,
                                                                           LocalDate checkin,
                                                                           LocalDate checkout,
                                                                           Integer numGuests,
                                                                           int page,
                                                                           int size,
                                                                           String sort) {
        if (filterByAvailability) {
            // 체크인, 체크아웃, 게스트 수가 제공된 경우
            if ("averageRating".equalsIgnoreCase(sort)) {
                return accommodationService.getAvailableAccommodationsByAverageRating(
                        areaCode != null ? String.valueOf(areaCode) : null, checkin, checkout, numGuests, page, size);
            } else if ("priceDesc".equalsIgnoreCase(sort)) {
                return accommodationService.getAvailableAccommodationsOrderByPriceDesc(
                        areaCode != null ? String.valueOf(areaCode) : null, checkin, checkout, numGuests, page, size);
            } else if ("priceAsc".equalsIgnoreCase(sort)) {
                return accommodationService.getAvailableAccommodationsOrderByPriceAsc(
                        areaCode != null ? String.valueOf(areaCode) : null, checkin, checkout, numGuests, page, size);
            } else {
                return accommodationService.getAvailableAccommodations(
                        areaCode != null ? String.valueOf(areaCode) : null, checkin, checkout, numGuests, page, size);
            }
        } else {
            // 필터링 없이 조회 (정렬 적용)
            if ("averageRating".equalsIgnoreCase(sort)) {
                return accommodationService.getAvailableAccommodationsSortedByRatingAndReview(PageRequest.of(page, size));
            } else if ("priceDesc".equalsIgnoreCase(sort)) {
                return areaCode != null
                        ? accommodationService.getAllAccommodationsOrderByPriceDesc(page, size)
                        : accommodationService.getAllAccommodationsOrderByPriceDesc(page, size);
            } else if ("priceAsc".equalsIgnoreCase(sort)) {
                return areaCode != null
                        ? accommodationService.getAllAccommodationsOrderByPriceAsc(page, size)
                        : accommodationService.getAllAccommodationsOrderByPriceAsc(page, size);
            } else {
                return areaCode != null
                        ? accommodationService.getAccommodationsByAreaCode(String.valueOf(areaCode), page, size)
                        : accommodationService.getAccommodations(page, size);
            }
        }
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



    @GetMapping("/search")
    public String searchByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        log.info("제목으로 검색합니다. 제목: {}, 페이지: {}, 사이즈: {}", title, page, size);

        // 페이지 설정
        page = Math.max(page, 0);
        size = Math.max(size, 1);

        // 페이지 요청 설정
        Pageable pageable = PageRequest.of(page, size);

        // 제목으로 검색
        Page<Accommodation> accommodationsPage = accommodationService.searchByTitle(title, pageable);

        List<Accommodation> accommodations = accommodationsPage.getContent();
        int totalPages = accommodationsPage.getTotalPages();
        int currentPage = page;

        // 페이지 번호 범위 계산
        int startPage = Math.max(0, currentPage - 5);
        int endPage = Math.min(totalPages - 1, currentPage + 5);

        model.addAttribute("accommodations", accommodations);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("pageSize", size);
        model.addAttribute("title", title);
        model.addAttribute("searchType", "title");

        log.info("현재 페이지: {}, 전체 페이지: {}, 시작 페이지: {}, 끝 페이지: {}", currentPage, totalPages, startPage, endPage);

        return "/accommodation/accommodations"; // 뷰 이름
    }



    @GetMapping("/reviews/{id}")
    @ResponseBody
    public ResponseEntity<Review> getReviewDetails(@PathVariable Long id) {
        Review review = reviewService.findReviewById(id);
        if (review == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(review);
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


    @GetMapping("/registerForm")
    public String accommodationRegister(
            Model model,
            @CookieValue(value = "accessToken", required = false) String accessToken
    ) {


        AccommodationRegisterDTO accommodationRegisterDTO = new AccommodationRegisterDTO();
        RoomDTO roomDTO = new RoomDTO();

        model.addAttribute("accommodationRegisterDTO", accommodationRegisterDTO);
        model.addAttribute("roomDTO", roomDTO);

        return "/accommodation/accommodationRegister";

    }

    @PostMapping("/register")
    public ResponseEntity<String> registerAccommodation(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @ModelAttribute AccommodationRegisterDTO dto) throws IOException {
        UserDetails userDetails = validateAndGetUserDetails(accessToken);
        Member member = memberService.findByEmail(userDetails.getUsername());
        accommodationService.registerAccommodation(member.getId(), dto);
        log.info("숙소 저장 성공");
        return ResponseEntity.ok("숙소 저장 성공");
    }


    private UserDetails validateAndGetUserDetails(String accessToken) {
        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            return null; // 인증 오류 시 null 반환
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        return (UserDetails) authentication.getPrincipal(); // 인증 성공 시 UserDetails 반환
    }


}

