package hello.yuhanTrip.controller.view;

import hello.yuhanTrip.domain.accommodation.*;
import hello.yuhanTrip.dto.accommodation.AccommodationDTO;
import hello.yuhanTrip.dto.accommodation.AccommodationRegisterDTO;
import hello.yuhanTrip.dto.accommodation.RoomDTO;
import hello.yuhanTrip.mapper.AccommodationMapper;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
import hello.yuhanTrip.service.member.MemberService;
import hello.yuhanTrip.service.reservation.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/accommodation")
@Log4j2
public class AccommodationViewController {

    private final AccommodationService accommodationService;
    private final MemberService memberService;
    private final ReviewService reviewService;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    // @GetMapping("/byregion") 리팩토링
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

        log.info("지역 코드로 숙소 리스트를 조회합니다. 지역: {}, 페이지: {}, 사이즈: {}, 체크인 날짜: {}, 체크아웃 날짜: {}, 숙박 인원 수: {}, 정렬: {}, 타입 : {} ",
                region, page, size, checkin, checkout, numGuests, sort, type);

        page = Math.max(page, 0);
        size = Math.max(size, 1);

        Page<Accommodation> accommodationsPage;

        Integer areaCode = (region != null && !region.isEmpty()) ? RegionCode.getCodeByRegion(region) : null;

        if (type != null && checkin == null && checkout == null && numGuests == null && areaCode == null) {
            accommodationsPage = accommodationService.fetchAccommodationsWithSortingAndFiltering(
                    type, null, false, checkin, checkout, numGuests, page, size, sort);
        } else if (checkin != null && checkout != null && numGuests != null && type != null) {
            accommodationsPage = accommodationService.fetchAccommodationsWithSortingAndFiltering(
                    type, areaCode, true, checkin, checkout, numGuests, page, size, sort);
        } else {
            accommodationsPage = accommodationService.fetchAccommodationsWithSortingAndFiltering(
                    type, areaCode, false, checkin, checkout, numGuests, page, size, sort);
        }


        // 페이지 처리 메소드 호출
        addPagingAttributes(model, accommodationsPage, page, size);

        String checkinFormatted = checkin != null ? checkin.format(formatter) : null;
        String checkoutFormatted = checkout != null ? checkout.format(formatter) : null;

        model.addAttribute("region", region);
        model.addAttribute("checkin", checkinFormatted);
        model.addAttribute("checkout", checkoutFormatted);
        model.addAttribute("numGuests", numGuests);
        model.addAttribute("sort", sort);
        model.addAttribute("searchType", "region");
        model.addAttribute("type", type != null ? type.name() : "");

        return "accommodation/accommodations";
    }

    // @GetMapping("/search") 리팩토링
    @GetMapping("/search")
    public String searchByTitle(@RequestParam String title,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "12") int size,
                                Model model) {

        log.info("제목으로 검색합니다. 제목: {}, 페이지: {}, 사이즈: {}", title, page, size);

        page = Math.max(page, 0);
        size = Math.max(size, 1);

        Pageable pageable = PageRequest.of(page, size);
        Page<Accommodation> accommodationsPage = accommodationService.searchByTitle(title, pageable);

        // 페이지 처리 메소드 호출
        addPagingAttributes(model, accommodationsPage, page, size);

        model.addAttribute("title", title);
        model.addAttribute("searchType", "title");

        return "accommodation/accommodations";
    }

    // @GetMapping("/info") 리팩토링
    @GetMapping("/info")
    public String getAccommodationInfo(@RequestParam("id") Long id,
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

        return "accommodation/accommodationInfo";
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

        memberService.validateHost(accessToken);

        model.addAttribute("accommodationRegisterDTO", new AccommodationRegisterDTO());
        model.addAttribute("roomDTO", new RoomDTO());

        return "accommodation/accommodationRegister";
    }


    // 페이지 처리 및 DTO 변환 메소드 추가
    private void addPagingAttributes(Model model, Page<Accommodation> accommodationsPage, int currentPage, int size) {
        int totalPages = accommodationsPage.getTotalPages();
        int startPage = Math.max(0, currentPage - 5);
        int endPage = Math.min(totalPages - 1, currentPage + 5);

        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("pageSize", size);

        List<AccommodationDTO> accommodationDTOs = AccommodationMapper.INSTANCE.toDTOList(accommodationsPage.getContent());
        model.addAttribute("accommodations", accommodationDTOs);
    }

    @GetMapping("/locations")
    public String getAllAccommodationLocations() {

        return "accommodation/mapsAccommodations";

    }

}