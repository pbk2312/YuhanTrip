package hello.yuhanTrip.controller;


import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.service.Accomodation.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;
    private final TokenProvider tokenProvider;

    @PostMapping("/{accommodationId}")
    public ResponseEntity<Void> likeAccommodation(@PathVariable Long accommodationId,
                                                  @CookieValue(value = "accessToken", required = false) String accessToken)
        {


            Authentication authentication = tokenProvider.getAuthentication(accessToken);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 인증 확인
            if (accessToken == null || !tokenProvider.validate(accessToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String userEmail = userDetails.getUsername();


            likeService.likeAccommodation(userEmail, accommodationId);
            return ResponseEntity.ok().build();

        }
        @GetMapping("/{accommodationId}")
        public ResponseEntity<Long> getLikeCount (@PathVariable Long accommodationId){
            long likeCount = likeService.getLikeCount(accommodationId);
            return ResponseEntity.ok(likeCount);
        }


    }