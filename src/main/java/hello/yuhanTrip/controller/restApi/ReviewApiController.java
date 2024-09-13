package hello.yuhanTrip.controller.restApi;

import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.domain.reservation.Reservation;
import hello.yuhanTrip.exception.UnauthorizedException;
import hello.yuhanTrip.service.reservation.ReservationService;
import hello.yuhanTrip.service.member.MemberService;
import hello.yuhanTrip.service.reservation.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@Log4j2
@RequiredArgsConstructor
public class ReviewApiController {


    private final MemberService memberService;
    private final ReservationService reservationService;
    private final ReviewService reviewService;
    // 리뷰 제출
    @PostMapping("/submitReview")
    public ResponseEntity<String> submitReview(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam("reservationId") Long reservationId,
            @RequestParam("rating") int rating,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "images", required = false) List<MultipartFile> images
    ) {
        try {
            log.info("리뷰 제출 요청: 예약ID={}, 평가={}, 내용={}, 이미지 개수={}", reservationId, rating, content, images != null ? images.size() : 0);

            Member member = memberService.getUserDetails(accessToken);
            Reservation reservation = reservationService.findReservation(reservationId);
            if (reservation == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("해당 예약 정보를 찾을 수 없습니다.");
            }

            reviewService.addReviewWithImages(
                    reservation.getAccommodationId(),
                    member.getEmail(),
                    reservationId,
                    content,
                    rating,
                    images
            );

            return ResponseEntity.ok("리뷰가 성공적으로 제출되었습니다.");
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("인증되지 않은 사용자입니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("이미지 업로드 중 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("리뷰 제출 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("리뷰 제출 중 예상치 못한 오류가 발생했습니다.");
        }
    }
}
