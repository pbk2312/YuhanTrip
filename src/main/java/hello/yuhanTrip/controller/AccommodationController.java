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
        // 페이지 번호와 사이즈 검증
        if (page < 0) page = 0;
        if (size <= 0) size = 10;

        Page<Accommodation> accommodationsPage = accommodationService.getAccommodations(page, size);

        // 페이지 번호 검증
        int currentPage = Math.max(page, 0); // Ensure page number is not negative
        if (accommodationsPage.getTotalPages() > 0 && currentPage >= accommodationsPage.getTotalPages()) {
            currentPage = accommodationsPage.getTotalPages() - 1;
        }

        model.addAttribute("accommodations", accommodationsPage.getContent());
        model.addAttribute("pageNumber", currentPage);
        model.addAttribute("totalPages", accommodationsPage.getTotalPages());
        model.addAttribute("pageSize", size);
        return "accommodations";
    }
}
