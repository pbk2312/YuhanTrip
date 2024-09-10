package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.service.member.MemberLikeService;
import hello.yuhanTrip.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
@Log4j2
public class MemberLikeController {

    private final MemberLikeService memberLikeService;
    private final MemberService memberService;
    private final TokenProvider tokenProvider;


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

        // 토큰 검증 및 유저 정보 가져오기
        UserDetails userDetails = validateAndGetUserDetails(accessToken);
        if (userDetails == null) {
            return createErrorResponse("인증오류", HttpStatus.UNAUTHORIZED, false);
        }

        // UserDetails에서 이메일을 통해 Member 객체 가져오기
        Member member = memberService.findByEmail(userDetails.getUsername());
        if (member == null) {
            return createErrorResponse("회원 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND, false);
        }

        // 좋아요 상태 조회
        boolean isLiked = memberLikeService.isLiked(member, accommodationId);

        // 응답 데이터 생성
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("isLiked", isLiked);
        return ResponseEntity.ok(response);
    }

    private UserDetails validateAndGetUserDetails(String accessToken) {
        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            return null; // 인증 오류 시 null 반환
        }
        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        return (UserDetails) authentication.getPrincipal(); // 인증 성공 시 UserDetails 반환
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status, boolean isLiked) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
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