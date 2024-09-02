package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.*;
import hello.yuhanTrip.dto.AccommodationRegisterDTO;
import hello.yuhanTrip.dto.RoomDTO;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.service.Accomodation.AccommodationServiceImpl;
import hello.yuhanTrip.service.MemberLikeService;
import hello.yuhanTrip.service.MemberService;
import hello.yuhanTrip.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    private final AccommodationServiceImpl accommodationService;
    private final TokenProvider tokenProvider;
    private final MemberService memberService;
    private final ReviewService reviewService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @GetMapping("/byregion")
    public String listAccommodationsByRegion(Model model,
                                             @RequestParam(value = "region", required = false) String region,
                                             @RequestParam(value = "checkin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkin,
                                             @RequestParam(value = "checkout", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkout,
                                             @RequestParam(value = "type", required = false) AccommodationType type,
                                             @RequestParam(value = "numGuests", required = false) Integer numGuests,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "12") int size,
                                             @RequestParam(required = false) String sort) {

        log.info("지역 코드로 숙소 리스트를 조회합니다. 지역: {}, 페이지: {}, 사이즈: {}, 체크인 날짜: {}, 체크아웃 날짜: {}, 숙박 인원 수: {}, 정렬: {}, 타입: {}",
                region, page, size, checkin, checkout, numGuests, sort, type);

        page = Math.max(page, 0);
        size = Math.max(size, 1);

        Integer areaCode = (region != null && !region.isEmpty()) ? RegionCode.getCodeByRegion(region) : null;

        Page<Accommodation> accommodationsPage = fetchAccommodationsWithSortingAndFiltering(
                type, areaCode, checkin != null && checkout != null && numGuests != null, checkin, checkout, numGuests, page, size, sort
        );

        int totalPages = accommodationsPage.getTotalPages();
        int currentPage = page;
        int startPage = Math.max(0, currentPage - 5);
        int endPage = Math.min(totalPages - 1, currentPage + 5);

        model.addAttribute("accommodations", accommodationsPage.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("pageSize", size);
        model.addAttribute("region", region);
        model.addAttribute("checkin", checkin != null ? checkin.format(formatter) : null);
        model.addAttribute("checkout", checkout != null ? checkout.format(formatter) : null);
        model.addAttribute("numGuests", numGuests);
        model.addAttribute("sort", sort);
        model.addAttribute("searchType", "region");
        model.addAttribute("type", type != null ? type.name() : "");

        log.info("현재 페이지: {}, 전체 페이지: {}, 시작 페이지: {}, 끝 페이지: {}", currentPage, totalPages, startPage, endPage);

        return "/accommodation/accommodations";
    }

    private Page<Accommodation> fetchAccommodationsWithSortingAndFiltering(
            AccommodationType type,
            Integer areaCode,
            boolean filterByAvailability,
            LocalDate checkin,
            LocalDate checkout,
            Integer numGuests,
            int page,
            int size,
            String sort) {

        Pageable pageable = PageRequest.of(page, size);

        if (type != null) {
            return getSortedAccommodationsByType(type, sort, pageable, page, size);
        } else if (filterByAvailability) {
            return accommodationService.findAvailableAccommodationsByType(
                    type, areaCode != null ? String.valueOf(areaCode) : null, checkin, checkout, numGuests, sort != null ? sort.toUpperCase() : "DEFAULT", page, size
            );
        } else {
            return getSortedAccommodationsWithoutType(areaCode, sort, pageable, page, size);
        }
    }

    private Page<Accommodation> getSortedAccommodationsByType(AccommodationType type, String sort, Pageable pageable, int page, int size) {
        switch (sort != null ? sort.toLowerCase() : "default") {
            case "averagerating":
                return accommodationService.getAccommodationsByTypeSortedByRatingAndReview(type, pageable);
            case "pricedesc":
                return accommodationService.getAccommodationsByTypeOrderByPriceDesc(type, page, size);
            case "priceasc":
                return accommodationService.getAccommodationsByTypeOrderByPriceAsc(type, page, size);
            default:
                return accommodationService.getAccommodationsByTypeSortedByRatingAndReview(type, pageable);
        }
    }

    private Page<Accommodation> getSortedAccommodationsWithoutType(Integer areaCode, String sort, Pageable pageable, int page, int size) {
        switch (sort != null ? sort.toLowerCase() : "default") {
            case "averagerating":
                return accommodationService.getAvailableAccommodationsSortedByRatingAndReview(pageable);
            case "pricedesc":
                return accommodationService.getAllAccommodationsOrderByPriceDesc(page, size);
            case "priceasc":
                return accommodationService.getAllAccommodationsOrderByPriceAsc(page, size);
            default:
                return areaCode != null
                        ? accommodationService.getAccommodationsByAreaCode(String.valueOf(areaCode), page, size)
                        : accommodationService.getAccommodations(page, size);
        }
    }

    @GetMapping("/search")
    public String searchByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        log.info("제목으로 검색합니다. 제목: {}, 페이지: {}, 사이즈: {}", title, page, size);

        page = Math.max(page, 0);
        size = Math.max(size, 1);

        Pageable pageable = PageRequest.of(page, size);
        Page<Accommodation> accommodationsPage = accommodationService.searchByTitle(title, pageable);

        int totalPages = accommodationsPage.getTotalPages();
        int currentPage = page;
        int startPage = Math.max(0, currentPage - 5);
        int endPage = Math.min(totalPages - 1, currentPage + 5);

        model.addAttribute("accommodations", accommodationsPage.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("pageSize", size);
        model.addAttribute("title", title);
        model.addAttribute("searchType", "title");

        log.info("현재 페이지: {}, 전체 페이지: {}, 시작 페이지: {}, 끝 페이지: {}", currentPage, totalPages, startPage, endPage);

        return "/accommodation/accommodations";
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

        List<Room> availableRooms = (checkInDate == null || checkOutDate == null)
                ? accommodation.getRooms()
                : accommodationService.getAvailableRoomsByAccommodation(id, checkInDate, checkOutDate);

        List<Review> reviews = reviewService.getReviewsByAccommodation(id);

        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("checkin", checkInDate);
        model.addAttribute("checkout", checkOutDate);
        model.addAttribute("numGuests", numberOfGuests);
        model.addAttribute("accommodation", accommodation);
        model.addAttribute("reviews", reviews);

        return "/accommodation/accommodationInfo";
    }

    @GetMapping("/reviews/{id}")
    @ResponseBody
    public ResponseEntity<Review> getReviewDetails(@PathVariable Long id) {
        Review review = reviewService.findReviewById(id);
        return review != null ? ResponseEntity.ok(review) : ResponseEntity.notFound().build();
    }

    @GetMapping("/registerForm")
    public String accommodationRegister(
            Model model,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            return "redirect:/member/login";
        }

        model.addAttribute("accommodationRegisterDTO", new AccommodationRegisterDTO());
        model.addAttribute("roomDTO", new RoomDTO());

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
            return null;
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        return (UserDetails) authentication.getPrincipal();
    }
}