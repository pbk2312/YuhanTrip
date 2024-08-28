package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.service.Accomodation.AccommodationServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/home")
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final AccommodationServiceImpl accommodationService;

    @GetMapping("/homepage")
    public String homePage(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "5") int size,
                           @RequestParam(required = false) String areaCode,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
                           @RequestParam(defaultValue = "1") int numGuests) {



        // 인기 추천 숙소 가져오기
        Page<Accommodation> accommodations = accommodationService.getAvailableAccommodations(
                areaCode,             // 지역 코드
                checkInDate,          // 체크인 날짜
                checkOutDate,         // 체크아웃 날짜
                numGuests,            // 인원 수
                page,                 // 페이지 번호
                size,                 // 페이지 크기
                "RATING"
        );

        // 모델에 숙소 목록과 페이지 정보를 추가
        model.addAttribute("accommodations", accommodations.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", accommodations.getTotalPages());

        // 홈 페이지 템플릿으로 이동
        return "home";
    }

}
