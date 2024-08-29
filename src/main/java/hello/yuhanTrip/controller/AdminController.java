package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.admin.RoleChangeRequest;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
import hello.yuhanTrip.service.RoleChangeRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@Controller
@Log4j2
@RequiredArgsConstructor
public class AdminController {

    private final RoleChangeRequestService roleChangeRequestService;
    private final AccommodationService accommodationService;
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

        Page<Accommodation> accommodations = accommodationService.getPendingAccommodations(PageRequest.of(0, 10));
        model.addAttribute("accommodations", accommodations);
        model.addAttribute("requests", requests);
        return "/admin/manageMent"; // 관리자 페이지의 뷰 이름
    }

    @PostMapping("/admin/request/approve")
    public String approveRequest(
            @RequestParam("id") Long id,
            Model model) {
        roleChangeRequestService.approveRequest(id);
        return "redirect:/admin/manageMent";
    }

    @PostMapping("/admin/request/reject")
    public String rejectRequest(@RequestParam("id") Long id, String rejectionReason, Model model) {
        roleChangeRequestService.rejectRequest(id, rejectionReason);
        return "redirect:/admin/manageMent";
    }

    @PostMapping("/admin/accommodation/approve")
    public String approveAccommodation(@RequestParam("id") Long id, Model model) {
        accommodationService.approveAccommodation(id);
        return "redirect:/admin/manageMent";
    }

    @GetMapping("/admin/request/file/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) {
        RoleChangeRequest request = roleChangeRequestService.getRequestById(id);
        if (request == null || request.getAttachmentFilePath() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        File file = new File(request.getAttachmentFilePath());
        Resource resource = new FileSystemResource(file);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + request.getAttachmentFileName());
        headers.add(HttpHeaders.CONTENT_TYPE, request.getAttachmentFileType());

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}
