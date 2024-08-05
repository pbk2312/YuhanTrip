package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.RegionCode;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/accommodation")
@Log4j2
public class AccommodationController {

    private final AccommodationService accommodationService;
    private static final DateTimeFormatter CUSTOM_DATE_FORMAT = DateTimeFormatter.ofPattern("dd. MM. yy");


    @GetMapping("/accommodations")
    public String listAccommodations(Model model,
                                     @RequestParam(required = false) String region,
                                     @RequestParam(value = "checkin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkin,
                                     @RequestParam(value = "checkout", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkout,
                                     @RequestParam(value = "numGuests", required = false) Integer numGuests,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "12") int size) {

        log.info("숙소 리스트를 조회합니다.... 페이지: {}, 사이즈: {}, 지역: {}, 체크인 날짜: {}, 체크아웃 날짜: {}, 숙박 인원 수: {}", page, size, region, checkin, checkout, numGuests);

        // 페이지 번호와 사이즈 검증
        page = Math.max(page, 0);
        size = Math.max(size, 1);

        Page<Accommodation> accommodationsPage;

        if (region != null && !region.isEmpty()) {
            // 지역 코드로 숙소 리스트 조회
            Integer areaCode = RegionCode.getCodeByRegion(region);
            if (areaCode == null) {
                log.error("잘못된 지역 이름: {}", region);
                return "error"; // 잘못된 지역 이름 처리
            }

            // 체크인, 체크아웃, 숙박 인원 수가 널이 아닌 경우 필터링을 포함한 조회
            if (checkin != null && checkout != null && numGuests != null) {
                accommodationsPage = accommodationService.getAvailableAccommodations(
                        String.valueOf(areaCode), checkin, checkout, numGuests, page, size);
            } else {
                // 필터링 없이 전체 숙소 조회
                accommodationsPage = accommodationService.getAccommodationsByAreaCode(String.valueOf(areaCode), page, size);
            }

            model.addAttribute("region", region);
        } else {
            // 전체 숙소 리스트 조회
            if (checkin != null && checkout != null && numGuests != null) {
                accommodationsPage = accommodationService.getAvailableAccommodations(
                        null, checkin, checkout, numGuests, page, size);
            } else {
                accommodationsPage = accommodationService.getAccommodations(page, size);
            }
        }

        int totalPages = accommodationsPage.getTotalPages();
        int currentPage = page;

        // 페이지 번호 범위 계산
        int startPage = Math.max(0, currentPage - 5);
        int endPage = Math.min(totalPages - 1, currentPage + 5);

        model.addAttribute("accommodations", accommodationsPage.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("pageSize", size);

        // 필터링 조건을 모델에 추가
        model.addAttribute("checkin", checkin);
        model.addAttribute("checkout", checkout);
        model.addAttribute("numGuests", numGuests);

        log.info("현재 페이지: {}, 전체 페이지: {}, 시작 페이지: {}, 끝 페이지: {}", currentPage, totalPages, startPage, endPage);

        return "accommodations"; // 뷰 이름
    }



    @GetMapping("/byregion")
    public String listAccommodationsByRegion(Model model,
                                             @RequestParam(value = "region", required = false) String region,
                                             @RequestParam(value = "checkin", required = false)  String checkInDateStr,
                                             @RequestParam(value = "checkout", required = false)  String checkOutDateStr,
                                             @RequestParam(value = "numGuests", required = false) Integer numGuests,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "12") int size) {

        LocalDate checkInDate = parseDate(checkInDateStr);
        LocalDate checkOutDate = parseDate(checkOutDateStr);

        log.info("지역 코드로 숙소 리스트를 조회합니다. 지역: {}, 페이지: {}, 사이즈: {}, 체크인 날짜: {}, 체크아웃 날짜: {}, 숙박 인원 수: {}", region, page, size, checkInDate, checkOutDate, numGuests);

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

            if (checkInDate != null && checkOutDate != null && numGuests != null) {
                // 체크인, 체크아웃, 숙박 인원 수가 모두 제공된 경우
                accommodationsPage = accommodationService.getAvailableAccommodations(
                        String.valueOf(areaCode), checkInDate, checkOutDate, numGuests, page, size);
            } else {
                // 필터링 없이 전체 숙소 조회
                accommodationsPage = accommodationService.getAccommodationsByAreaCode(
                        String.valueOf(areaCode), page, size);
            }

            model.addAttribute("region", region);
        } else {
            // 지역 코드가 제공되지 않은 경우
            if (checkInDate != null && checkOutDate != null && numGuests != null) {
                // 체크인, 체크아웃, 숙박 인원 수가 모두 제공된 경우
                accommodationsPage = accommodationService.getAvailableAccommodations(
                        null, checkInDate, checkOutDate, numGuests, page, size);
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

        model.addAttribute("accommodations", accommodationsPage.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("pageSize", size);
        model.addAttribute("region", region);
        model.addAttribute("checkin", checkInDateStr);
        model.addAttribute("checkout", checkOutDateStr);
        model.addAttribute("numGuests", numGuests);

        log.info("현재 페이지: {}, 전체 페이지: {}, 시작 페이지: {}, 끝 페이지: {}", currentPage, totalPages, startPage, endPage);

        return "accommodations"; // 뷰 이름
    }





    @GetMapping("/info")
    public String getAccommodationInfo(@RequestParam("id") Long id, Model model,
                                       @RequestParam(value = "checkInDate", required = false) String checkInDateStr,
                                       @RequestParam(value = "checkOutDate", required = false) String checkOutDateStr,
                                       @RequestParam(value = "numberOfGuests", required = false) Integer numberOfGuests) {
        LocalDate checkInDate = parseDate(checkInDateStr);
        LocalDate checkOutDate = parseDate(checkOutDateStr);

        log.info("숙소 정보를 가져옵니다... ID: {}", id);
        log.info("선택 사항 - 체크인 날짜: {}, 체크아웃 날짜: {}, 숙박 인원 수: {}", checkInDate, checkOutDate, numberOfGuests);

        // 숙소 정보를 가져옵니다.
        Accommodation accommodation = accommodationService.getAccommodationInfo(id);

        // 숙소 정보가 존재하지 않는 경우
        if (accommodation == null) {
            log.error("숙소 정보를 찾을 수 없습니다. ID: {}", id);
            return "error"; // 또는 "redirect:/error/404"
        }

        // 모델에 숙소 정보 추가
        model.addAttribute("accommodation", accommodation);
        model.addAttribute("checkInDate", checkInDate);
        model.addAttribute("checkOutDate", checkOutDate);
        model.addAttribute("numberOfGuests", numberOfGuests);

        return "accommodationInfo"; // 상세 페이지의 뷰 이름
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dateStr, CUSTOM_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            log.error("날짜 포맷 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    // 날짜 포맷 예외 처리
    @ControllerAdvice
    public static class GlobalExceptionHandler {

        @ExceptionHandler(DateTimeParseException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public String handleDateTimeParseException(DateTimeParseException ex, Model model) {
            log.error("날짜 포맷 오류 발생: {}", ex.getMessage());
            model.addAttribute("errorMessage", "잘못된 날짜 포맷입니다. 날짜 형식은 dd. MM. yy 여야 합니다.");
            return "error"; // 또는 적절한 오류 페이지로 리다이렉트
        }
    }
}
