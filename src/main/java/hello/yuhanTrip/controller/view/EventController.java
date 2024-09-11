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

        if ((now.isAfter(LocalTime.of(22, 59)) && now.isBefore(LocalTime.of(23, 59))) || now.isBefore(LocalTime.of(0, 0))) {
            model.addAttribute("discountType", "fixed");
            model.addAttribute("discountPrice", 10000); // 10000원 고정 할인 쿠폰
        }
        else if (now.isAfter(LocalTime.of(18, 0)) && now.isBefore(LocalTime.of(19, 0))) {
            model.addAttribute("discountType", "percentage");
            model.addAttribute("discountPrice", 0.2); // 20% 할인 쿠폰
        }
        else {
            model.addAttribute("discountType", "none");
        }

        return "DiscountPopup";
    }
}