package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.admin.RoleChangeRequest;
import hello.yuhanTrip.service.RoleChangeRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@Log4j2
@RequiredArgsConstructor
public class AdminController {

    private final RoleChangeRequestService roleChangeRequestService;

    @GetMapping("/about")
    public String aboutPage() {
        return "about";
    }

    @GetMapping("/admin/manageMent")
    public String manageMentPage(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            Model model
    ) {
        Page<RoleChangeRequest> requests = roleChangeRequestService.getPendingRequests(PageRequest.of(0, 10));
        model.addAttribute("requests", requests);
        return "/admin/manageMent"; // 관리자 페이지의 뷰 이름
    }

    @PostMapping("/admin/request/{id}/approve")
    public String approveRequest(@PathVariable Long id, Model model) {
        roleChangeRequestService.approveRequest(id);
        return "redirect:/admin/manageMent";
    }

    @PostMapping("/admin/request/{id}/reject")
    public String rejectRequest(@PathVariable Long id, String rejectionReason, Model model) {
        roleChangeRequestService.rejectRequest(id, rejectionReason);
        return "redirect:/admin/manageMent";
    }
}