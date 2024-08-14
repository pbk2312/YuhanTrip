package hello.yuhanTrip.controller;


import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.service.MemberLikeService;
import hello.yuhanTrip.service.MemberService;
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
    private final TokenProvider tokenProvider;
    private final MemberService memberService;



    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addLike(@RequestParam Long accommodationId,
                                                       @CookieValue(value = "accessToken", required = false) String accessToken) {
        UserDetails userDetails = validateAndGetUserDetails(accessToken);
        Map<String, String> response = new HashMap<>();

        if (userDetails == null) {
            response.put("status", "error");
            response.put("message", "인증오류");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Member member = memberService.findByEmail(userDetails.getUsername());
        memberLikeService.addLike(member, accommodationId);

        response.put("status", "success");
        response.put("message", "Like added successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/remove")
    public ResponseEntity<Map<String, String>> removeLike(@RequestParam Long accommodationId,
                                                          @CookieValue(value = "accessToken", required = false) String accessToken) {
        UserDetails userDetails = validateAndGetUserDetails(accessToken);
        Map<String, String> response = new HashMap<>();

        if (userDetails == null) {
            response.put("status", "error");
            response.put("message", "인증오류");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Member member = memberService.findByEmail(userDetails.getUsername());
        memberLikeService.removeLike(member, accommodationId);

        response.put("status", "success");
        response.put("message", "Like removed successfully");
        return ResponseEntity.ok(response);
    }


    private UserDetails validateAndGetUserDetails(String accessToken) {
        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            return null; // 인증 오류 시 null 반환
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        return (UserDetails) authentication.getPrincipal(); // 인증 성공 시 UserDetails 반환
    }




    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkLikeStatus(@RequestParam Long accommodationId,
                                                               @CookieValue(value = "accessToken", required = false) String accessToken) {
        Map<String, Object> response = new HashMap<>();

        // 인증 확인
        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            response.put("status", "error");
            response.put("message", "인증오류");
            response.put("isLiked", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Member member = memberService.findByEmail(userDetails.getUsername());

        boolean isLiked = memberLikeService.isLiked(member, accommodationId);

        response.put("status", "success");
        response.put("isLiked", isLiked);
        return ResponseEntity.ok(response);
    }



}
