package hello.yuhanTrip.service;


import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.domain.Reservation;
import hello.yuhanTrip.domain.Review;
import hello.yuhanTrip.repository.AccommodationRepository;
import hello.yuhanTrip.repository.MemberRepository;
import hello.yuhanTrip.repository.ReviewRepository;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
import hello.yuhanTrip.service.Accomodation.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {


    private final ReviewRepository reviewRepository;
    private final AccommodationService accommodationService;
    private final MemberService memberService;
    private final ReservationService reservationService;


    public Review addReview(Long accommodationId, String memberEmail, Long reservationId, String content, int rating) {
        Accommodation accommodation = accommodationService.getAccommodationInfo(accommodationId);
        Member member = memberService.findByEmail(memberEmail);
        Reservation reservation = reservationService.findReservation(reservationId);

        // 예약 확인 및 이미 리뷰가 있는지 체크
        if (!reservation.getMember().equals(member) || !reservation.getAccommodationId().equals(accommodationId)) {
            throw new IllegalStateException("해당 예약이 존재하지 않거나 사용자가 이 예약에 대해 권한이 없습니다.");
        }

        if (reservation.getReview() != null) {
            throw new IllegalStateException("이 예약에 대해서는 이미 리뷰가 작성되었습니다.");
        }

        Review review = Review.builder()
                .accommodation(accommodation)
                .member(member)
                .reservation(reservation)
                .content(content)
                .rating(rating)
                .reviewDate(LocalDate.now())
                .build();

        // Review와 Reservation 간의 관계 설정
        reservation.setReview(review);

        return reviewRepository.save(review);
    }

    public List<Review> getReviewsForAccommodation(Long accommodationId) {
        return reviewRepository.findAll().stream()
                .filter(review -> review.getAccommodation().getId().equals(accommodationId))
                .collect(Collectors.toList());
    }

}
