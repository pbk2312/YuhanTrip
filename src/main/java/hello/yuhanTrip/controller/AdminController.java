package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.admin.RoleChangeRequest;
import hello.yuhanTrip.jwt.TokenProvider;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@Controller
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final RoleChangeRequestService roleChangeRequestService;
    private final AccommodationService accommodationService;
    private final TokenProvider tokenProvider;



    @GetMapping("/manageMent")
    public String manageMentPage(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            Model model
    ) {
        UserDetails userDetails = validateTokenAndGetUserDetails(accessToken);
        if (userDetails == null) {
            return "redirect:/login";
        }

        Page<RoleChangeRequest> requests = roleChangeRequestService.getPendingRequests(PageRequest.of(0, 10));
        Page<Accommodation> accommodations = accommodationService.getPendingAccommodations(PageRequest.of(0, 10));

        model.addAttribute("accommodations", accommodations);
        model.addAttribute("requests", requests);
        return "/admin/manageMent";
    }

    @PostMapping("/request/approve")
    public String approveRequest(@RequestParam("id") Long id) {
        roleChangeRequestService.approveRequest(id);
        return "redirect:/admin/manageMent";
    }

    @PostMapping("/request/reject")
    public String rejectRequest(@RequestParam("id") Long id, @RequestParam("rejectionReason") String rejectionReason) {
        roleChangeRequestService.rejectRequest(id, rejectionReason);
        return "redirect:/admin/manageMent";
    }

    @PostMapping("/accommodation/approve")
    public String approveAccommodation(@RequestParam("id") Long id) {
        accommodationService.approveAccommodation(id);
        return "redirect:/admin/manageMent";
    }

    @GetMapping("/request/file/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) {
        RoleChangeRequest request = roleChangeRequestService.getRequestById(id);
        if (request == null || request.getAttachmentFilePath() == null) {
            log.warn("File not found for request ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        File file = new File(request.getAttachmentFilePath());
        Resource resource = new FileSystemResource(file);

        HttpHeaders headers = createFileHeaders(request);

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    private UserDetails validateTokenAndGetUserDetails(String accessToken) {
        if (isInvalidToken(accessToken)) {
            log.info("Invalid access token.");
            return null;
        }
        return getUserDetails(accessToken);
    }

    private boolean isInvalidToken(String accessToken) {
        return accessToken == null || !tokenProvider.validate(accessToken);
    }

    private UserDetails getUserDetails(String accessToken) {
        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        return (UserDetails) authentication.getPrincipal();
    }

    private HttpHeaders createFileHeaders(RoleChangeRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + request.getAttachmentFileName());
        headers.add(HttpHeaders.CONTENT_TYPE, request.getAttachmentFileType());
        return headers;
    }
}