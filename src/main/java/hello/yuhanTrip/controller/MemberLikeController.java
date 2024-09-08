package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.service.member.MemberLikeService;
import hello.yuhanTrip.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
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

        Member member = memberService.getUserDetails(accessToken);
        memberLikeService.addLike(member, accommodationId);

        return createSuccessResponse("Like added successfully");
    }


    // 좋아요 기능 삭제
    @PostMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeLike(
            @RequestParam Long accommodationId,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        Member member = memberService.getUserDetails(accessToken);
        memberLikeService.removeLike(member, accommodationId);

        return createSuccessResponse("Like removed successfully");
    }


    // 좋아요 상태
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkLikeStatus(
            @RequestParam Long accommodationId,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        Member member = memberService.getUserDetails(accessToken);
        boolean isLiked = memberLikeService.isLiked(member, accommodationId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("isLiked", isLiked);
        return ResponseEntity.ok(response);
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