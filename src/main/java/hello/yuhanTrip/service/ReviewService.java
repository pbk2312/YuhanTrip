package hello.yuhanTrip.service;


import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.domain.Review;
import hello.yuhanTrip.repository.AccommodationRepository;
import hello.yuhanTrip.repository.MemberRepository;
import hello.yuhanTrip.repository.ReviewRepository;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
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

    public Review addReview(Long accommodationId,String memberEmail,String content, int rating) {
        Accommodation accommodation = accommodationService.getAccommodationInfo(accommodationId);
        Member member = memberService.findByEmail(memberEmail);

        Review review = Review.builder()
                .accommodation(accommodation)
                .member(member)
                .content(content)
                .rating(rating)
                .reviewDate(LocalDate.now())
                .build();

        return reviewRepository.save(review);
    }

    public List<Review> getReviewsForAccommodation(Long accommodationId) {
        return reviewRepository.findAll().stream()
                .filter(review -> review.getAccommodation().getId().equals(accommodationId))
                .collect(Collectors.toList());
    }

}
