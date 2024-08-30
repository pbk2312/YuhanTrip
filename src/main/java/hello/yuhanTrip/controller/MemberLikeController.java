package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.service.MemberLikeService;
import hello.yuhanTrip.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
@Log4j2
public class MemberLikeController {

    private final MemberLikeService memberLikeService;
    private final TokenProvider tokenProvider;
    private final MemberService memberService;


    // 좋아요 기능 추가
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addLike(
            @RequestParam Long accommodationId,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        UserDetails userDetails = validateAndGetUserDetails(accessToken);
        if (userDetails == null) {
            return createErrorResponse("인증오류", HttpStatus.UNAUTHORIZED);
        }

        Member member = memberService.findByEmail(userDetails.getUsername());
        memberLikeService.addLike(member, accommodationId);

        return createSuccessResponse("Like added successfully");
    }


    // 좋아요 기능 삭제
    @PostMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeLike(
            @RequestParam Long accommodationId,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        UserDetails userDetails = validateAndGetUserDetails(accessToken);
        if (userDetails == null) {
            return createErrorResponse("인증오류", HttpStatus.UNAUTHORIZED);
        }

        Member member = memberService.findByEmail(userDetails.getUsername());
        memberLikeService.removeLike(member, accommodationId);

        return createSuccessResponse("Like removed successfully");
    }


    // 좋아요 상태
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkLikeStatus(
            @RequestParam Long accommodationId,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        UserDetails userDetails = validateAndGetUserDetails(accessToken);
        if (userDetails == null) {
            return createErrorResponse("인증오류", HttpStatus.UNAUTHORIZED, false);
        }

        Member member = memberService.findByEmail(userDetails.getUsername());
        boolean isLiked = memberLikeService.isLiked(member, accommodationId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("isLiked", isLiked);
        return ResponseEntity.ok(response);
    }

    // 좋아요 목록 가져오기
    @GetMapping("/memberLikeHistory")
    public String memberLikeHistory(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "3") int size,
            Model model
    ) {
        // 인증 확인
        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            return "redirect:/member/login"; // 로그인 페이지로 리다이렉트
        }

        try {
            // 페이지 번호와 사이즈 검증
            page = Math.max(page, 0); // 페이지 번호는 음수일 수 없음
            size = Math.max(size, 1); // 페이지 사이즈는 1 이상이어야 함

            Authentication authentication = tokenProvider.getAuthentication(accessToken);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Member member = memberService.findByEmail(userDetails.getUsername());

            Pageable pageable = PageRequest.of(page, size);
            Page<Accommodation> likesByMember = memberLikeService.getLikesByMember(member, pageable);

            model.addAttribute("likesByMember", likesByMember);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", likesByMember.getTotalPages());
            model.addAttribute("pageSize", size); // 페이지 사이즈 모델에 추가

            return "/accommodation/likesByMember";
        } catch (Exception e) {
            log.error("Error processing /memberLikeHistory request", e);
            return "error"; // 오류 페이지로 리다이렉트
        }
    }

    private UserDetails validateAndGetUserDetails(String accessToken) {
        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            return null; // 인증 오류 시 null 반환
        }
        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        return (UserDetails) authentication.getPrincipal(); // 인증 성공 시 UserDetails 반환
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(createResponse("error", message));
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status, boolean isLiked) {
        Map<String, Object> response = createResponse("error", message);
        response.put("isLiked", isLiked);
        return ResponseEntity.status(status).body(response);
    }

    private ResponseEntity<Map<String, Object>> createSuccessResponse(String message) {
        return ResponseEntity.ok(createResponse("success", message));
    }

    private Map<String, Object> createResponse(String status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("message", message);
        return response;
    }
}