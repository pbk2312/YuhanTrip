package hello.yuhanTrip.controller.view;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalTime;

@Controller
@RequiredArgsConstructor
@Log4j2
public class EventController {


    @GetMapping("/discountPopup")
    public String discountPopup(Model model) {
        LocalTime now = LocalTime.now();
        log.info("현재 시간: {}", now); // 현재 시간 로그 출력

        if (now.isAfter(LocalTime.of(0, 0)) && now.isBefore(LocalTime.of(0, 10))) {
            // 고정 금액 할인 쿠폰
            model.addAttribute("discountType", "fixed");
            model.addAttribute("discountPrice", 10000); // 10000원 고정 할인 쿠폰
        } else if (now.isAfter(LocalTime.of(18, 0)) && now.isBefore(LocalTime.of(18, 10))) {
            // 비율 할인 쿠폰
            model.addAttribute("discountType", "percentage");
            model.addAttribute("discountPrice", 20); // 20% 할인 쿠폰을 소수점 대신 정수로 설정
        } else {
            model.addAttribute("discountType", "none");
        }

        log.info("Model에 전달된 discountType: {}", model.getAttribute("discountType"));
        log.info("Model에 전달된 discountPrice: {}", model.getAttribute("discountPrice"));

        return "DiscountPopup";
    }
}