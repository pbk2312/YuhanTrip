package hello.yuhanTrip.controller.view;



import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequiredArgsConstructor
public class TestController {



    @GetMapping("/test")
    public String test() {
        return "mapsAccommodations";
    }


}
