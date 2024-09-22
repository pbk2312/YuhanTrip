package hello.yuhanTrip.controller.view;

import hello.yuhanTrip.domain.accommodation.Accommodation;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.domain.reservation.Reservation;
import hello.yuhanTrip.domain.accommodation.Review;
import hello.yuhanTrip.dto.accommodation.ReservationDTO;
import hello.yuhanTrip.dto.accommodation.ReviewWriteDTO;
import hello.yuhanTrip.service.Accomodation.AccommodationServiceImpl;
import hello.yuhanTrip.service.reservation.ReservationService;
import hello.yuhanTrip.service.member.MemberService;
import hello.yuhanTrip.service.reservation.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@Log4j2
public class ReviewController {

    private final ReviewService reviewService;
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
