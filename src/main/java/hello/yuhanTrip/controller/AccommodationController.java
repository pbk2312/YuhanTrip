package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/accommodation")
@Log4j2
public class AccommodationController {

    private final AccommodationService accommodationService;

    @GetMapping("/accommodations")
    public String listAccommodations(Model model,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {

        log.info("숙소 리스트를 조회합니다.... 페이지: {}, 사이즈: {}", page, size);

        // 페이지 번호와 사이즈 검증
        page = Math.max(page, 0);
        size = Math.max(size, 1);

        Page<Accommodation> accommodationsPage = accommodationService.getAccommodations(page, size);

        int totalPages = accommodationsPage.getTotalPages();
        int currentPage = page;

        model.addAttribute("accommodations", accommodationsPage.getContent());
        model.addAttribute("pageNumber", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);
        log.info("현재 페이지: {}, 전체 페이지: {}", currentPage, totalPages);

        return "accommodations";
    }
}
