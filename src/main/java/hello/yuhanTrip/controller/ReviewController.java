package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.domain.Reservation;
import hello.yuhanTrip.domain.Review;
import hello.yuhanTrip.dto.ReservationDTO;
import hello.yuhanTrip.dto.ReviewWriteDTO;
import hello.yuhanTrip.exception.UnauthorizedException;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.service.Accomodation.AccommodationServiceImpl;
import hello.yuhanTrip.service.Accomodation.ReservationService;
import hello.yuhanTrip.service.MemberService;
import hello.yuhanTrip.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Log4j2
public class ReviewController {

    private final ReviewService reviewService;
    private final TokenProvider tokenProvider;
    private final MemberService memberService;
    private final ReservationService reservationService;
    private final AccommodationServiceImpl accommodationService;


    // 리뷰 쓰기
    @GetMapping("/reviewWrite")
    public String showReviewWrite(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam("reservationId") Long reservationId,
            Model model
    ) {
        try {

            // 로그인한 사용자의 정보를 가져옴
            Member member = memberService.getUserDetails(accessToken);
            Reservation reservation = reservationService.findReservation(reservationId);
            Accommodation accommodationInfo = accommodationService.getAccommodationInfo(reservation.getAccommodationId());

            ReservationDTO reservationDTO = ReservationDTO.builder()
                    .id(reservation.getId())
                    .memberId(reservation.getMember().getId())
                    .accommodationId(reservation.getAccommodationId())
                    .roomId(reservation.getRoom().getId())
                    .roomNm(reservation.getRoom().getRoomNm())
                    .roomType(reservation.getRoom().getRoomType())
                    .checkInDate(reservation.getCheckInDate())
                    .checkOutDate(reservation.getCheckOutDate())
                    .reservationDate(reservation.getReservationDate())
                    .specialRequests(reservation.getSpecialRequests())
                    .accommodationTitle(accommodationInfo.getTitle())
                    .name(reservation.getName())
                    .phoneNumber(reservation.getPhoneNumber())
                    .addr(reservation.getAddr())
                    .numberOfGuests(reservation.getNumberOfGuests())
                    .price(reservation.getPayment().getPrice())
                    .build();

            ReviewWriteDTO reviewWriteDTO = ReviewWriteDTO.builder()
                    .reviewDate(LocalDate.now())
                    .build();

            model.addAttribute("reviewWriteDTO", reviewWriteDTO);
            model.addAttribute("member", member);
            model.addAttribute("reservationDTO", reservationDTO);

            return "mypage/reviewWrite";
        } catch (Exception e) {
            log.error("리뷰 작성 페이지 로드 중 오류 발생", e);
            return "error"; // 오류 페이지로 리다이렉트
        }
    }


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



    // 리뷰 조회
    @GetMapping("/myReviews")
    public String getMyReviews(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size, // 한 페이지당 2개의 리뷰를 보여줌
            Model model
    ) {

        try {
            Member member = memberService.getUserDetails(accessToken);
            Page<Review> reviewsPage = reviewService.getReviewsByMemberWithPagination(member.getId(), page, size);



            model.addAttribute("reviews", reviewsPage.getContent());
            model.addAttribute("member", member);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", reviewsPage.getTotalPages());

            return "mypage/myReviews";
        } catch (Exception e) {
            log.error("내 리뷰 페이지 로드 중 오류 발생", e);
            return "error"; // 오류 페이지로 리다이렉트
        }
    }

}
