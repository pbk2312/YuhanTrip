package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/home")
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final AccommodationService accommodationService;

    @GetMapping("/homepage")
    public String homePage(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "5") int size) {
        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(page, size);

        // 평점과 리뷰 수로 정렬된 숙소 목록을 가져옴
        Page<Accommodation> accommodations = accommodationService.getAvailableAccommodationsSortedByRatingAndReview(pageable);

        // 모델에 숙소 목록과 페이지 정보를 추가
        model.addAttribute("accommodations", accommodations.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", accommodations.getTotalPages());

        // 홈 페이지 템플릿으로 이동
        return "home";
    }
}
