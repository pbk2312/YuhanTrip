package hello.yuhanTrip.controller;


import hello.yuhanTrip.domain.Accommodation;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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
        Map<String, String> response = new HashMap<>();

        // 인증 확인
        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            response.put("status", "error");
            response.put("message", "인증오류");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Member member = memberService.findByEmail(userDetails.getUsername());

        memberLikeService.addLike(member, accommodationId);

        response.put("status", "success");
        response.put("message", "Like added successfully");
        return ResponseEntity.ok(response);
    }


    @PostMapping("/remove")
    public ResponseEntity<Map<String, String>> removeLike(@RequestParam Long accommodationId,
                                                          @CookieValue(value = "accessToken", required = false) String accessToken) {
        Map<String, String> response = new HashMap<>();

        // 인증 확인
        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            response.put("status", "error");
            response.put("message", "인증오류");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Member member = memberService.findByEmail(userDetails.getUsername());

        memberLikeService.removeLike(member, accommodationId);

        response.put("status", "success");
        response.put("message", "Like removed successfully");
        return ResponseEntity.ok(response);
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
