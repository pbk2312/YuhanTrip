package hello.yuhanmarket.controller;


import hello.yuhanmarket.dto.schedule.ScheduleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/Schedule")
public class ScheduleController {

    @GetMapping("/create")
    public String createSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute ScheduleDTO scheduleDTO
            ){

    }

}
