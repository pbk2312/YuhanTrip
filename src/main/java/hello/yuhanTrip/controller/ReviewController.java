package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.domain.Reservation;
import hello.yuhanTrip.dto.ReservationDTO;
import hello.yuhanTrip.dto.ReviewWriteDTO;
import hello.yuhanTrip.exception.UnauthorizedException;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
import hello.yuhanTrip.service.Accomodation.ReservationService;
import hello.yuhanTrip.service.MemberService;
import hello.yuhanTrip.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@Log4j2
public class ReviewController {

    private final ReviewService reviewService;
    private final TokenProvider tokenProvider;
    private final MemberService memberService;
    private final ReservationService reservationService;
    private final AccommodationService accommodationService;

    @GetMapping("/reviewWrite")
    public String showReviewWrite(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam("reservationId") Long reservationId,
            Model model
    ) {
        // 로그인한 사용자의 정보를 가져옴
        Member member = getUserDetails(accessToken);
        Reservation reservation = reservationService.findReservation(reservationId);
        Accommodation accommodationInfo = accommodationService.getAccommodationInfo(reservation.getAccommodationId());

        ReservationDTO reservationDTO = ReservationDTO.builder()
                .id(reservation.getId())
                .memberId(reservation.getMember().getId())
                .accommodationId(reservation.getAccommodationId())
                .roomId(reservation.getRoom().getId())
                .roomNm(reservation.getRoom().getRoomNm()) // 예시: roomNumber는 Room 객체의 속성
                .roomType(reservation.getRoom().getRoomType()) // 예시: roomType은 Room 객체의 속성
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .reservationDate(reservation.getReservationDate())
                .specialRequests(reservation.getSpecialRequests())
                .accommodationTitle(accommodationInfo.getTitle()) // 숙소 제목을 가져오는 서비스 메서드 호출
                .name(reservation.getName())
                .phoneNumber(reservation.getPhoneNumber())
                .addr(reservation.getAddr())
                .numberOfGuests(reservation.getNumberOfGuests())
                .price(reservation.getPayment().getPrice()) // 예시: price는 Room 객체의 속성
                .build();

        // 사용자가 작성할 리뷰에 대한 기본 정보를 DTO에 담아 모델에 전달
        ReviewWriteDTO reviewWriteDTO = ReviewWriteDTO.builder()
                .reviewDate(LocalDate.now()) // 리뷰 작성일 기본값 설정
                .build();

        // 모델에 DTO와 기타 필요한 데이터 담기
        model.addAttribute("reviewWriteDTO", reviewWriteDTO);
        model.addAttribute("member", member);
        model.addAttribute("reservationDTO", reservationDTO);

        return "/mypage/reviewWrite"; // reviewWrite.html 뷰로 이동
    }


    @PostMapping("/submitReview")
    public ResponseEntity<?> submitReview(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam("reservationId") Long reservationId,
            @RequestParam("rating") int rating,
            @RequestParam(value = "content", required = false) String content
    ) {
        log.info("폼 제출");
        try {
            // 로그로 content 확인
            log.info("content : {} " ,content);
            // 로그인한 사용자의 정보를 가져옴
            Member member = getUserDetails(accessToken);

            // 예약 정보를 가져옴
            Reservation reservation = reservationService.findReservation(reservationId);
            if (reservation == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("예약 정보를 찾을 수 없습니다.");
            }

            // 리뷰 추가 서비스 호출
            reviewService.addReview(
                    reservation.getAccommodationId(),
                    member.getEmail(),
                    reservationId,
                    content,
                    rating
            );

            // 성공 메시지 반환
            return ResponseEntity.status(HttpStatus.OK)
                    .body("리뷰가 성공적으로 제출되었습니다.");
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("인증되지 않은 사용자입니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("리뷰 제출 중 오류가 발생했습니다.");
        }
    }



    public Member getUserDetails(String accessToken) {
        if (accessToken == null || !tokenProvider.validate(accessToken)) {
            throw new UnauthorizedException("인증되지 않은 사용자입니다.");
        }

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.info("리뷰 작성 시도 유저 : {}", userDetails.getUsername());

        return memberService.findByEmail(userDetails.getUsername());
    }
}
