package hello.yuhanTrip.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/inquiries")
public class InquiryViewController {

    @GetMapping
    public String showInquiryForm() {
        // inquiryForm.html 템플릿으로 이동
        return "inquiryForm";
    }
}