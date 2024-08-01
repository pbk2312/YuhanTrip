package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.*;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.repository.PaymentRepository;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
import hello.yuhanTrip.service.Accomodation.ReservationService;
import hello.yuhanTrip.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequiredArgsConstructor
@RequestMapping("/accommodation")
@Log4j2
public class AccommodationController {

    private final AccommodationService accommodationService;


    @GetMapping("/accommodations")
    public String listAccommodations(Model model,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "12") int size
    ) {

        log.info("숙소 리스트를 조회합니다.... 페이지: {}, 사이즈: {}", page, size);

        // 페이지 번호와 사이즈 검증
        page = Math.max(page, 0);
        size = Math.max(size, 1);

        Page<Accommodation> accommodationsPage = accommodationService.getAccommodations(page, size);

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

        log.info("현재 페이지: {}, 전체 페이지: {}, 시작 페이지: {}, 끝 페이지: {}", currentPage, totalPages, startPage, endPage);

        return "accommodations";
    }


    @GetMapping("/info")
    public String getAccommodationInfo(@RequestParam("id") Long id, Model model) {


        log.info("숙소 정보를 가져옵니다... = {}", id);

        // 숙소 정보를 가져옵니다.
        Accommodation accommodation = accommodationService.getAccommodationInfo(id);

        // 숙소 정보가 존재하지 않는 경우 에러 페이지로 리다이렉트 또는 404 오류 페이지로 이동
        if (accommodation == null) {
            return "error/404"; // 또는 "redirect:/error/404"
        }

        // 모델에 숙소 정보를 추가하여 뷰로 전달합니다.
        model.addAttribute("accommodation", accommodation);

        // 상세 페이지로 이동합니다.
        return "accommodationInfo"; // 상세 페이지의 뷰 이름

    }


}
