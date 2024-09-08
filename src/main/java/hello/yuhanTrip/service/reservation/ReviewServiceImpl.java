package hello.yuhanTrip.service.reservation;

import hello.yuhanTrip.domain.*;
import hello.yuhanTrip.domain.accommodation.Accommodation;
import hello.yuhanTrip.domain.accommodation.Review;
import hello.yuhanTrip.domain.accommodation.ReviewImage;
import hello.yuhanTrip.domain.reservation.Reservation;
import hello.yuhanTrip.repository.AccommodationRepository;
import hello.yuhanTrip.repository.ReviewImageRepository;
import hello.yuhanTrip.repository.ReviewRepository;
import hello.yuhanTrip.service.Accomodation.AccommodationServiceImpl;
import hello.yuhanTrip.service.Accomodation.ReservationService;
import hello.yuhanTrip.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Log4j2
@Service
public class ReviewServiceImpl implements ReviewService {


    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final AccommodationRepository accommodationRepository;
    private final AccommodationServiceImpl accommodationService;
    private final MemberService memberService;
    private final ReservationService reservationService;


    @Value("${upload.dir}")
    private String uploadDir;  // 주입된 업로드 디렉토리 경로


    public Review addReviewWithImages(Long accommodationId, String memberEmail, Long reservationId, String content, int rating, List<MultipartFile> images) throws IOException {
        log.info("리뷰 추가 시작: accommodationId={}, memberEmail={}, reservationId={}, rating={}", accommodationId, memberEmail, reservationId, rating);

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
        Review savedReview = reviewRepository.save(review);

        log.info("이미지 저장 시작.....");

        // 이미지 저장 및 ReviewImage 엔티티 생성
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty()) {
                    String imageUrl = saveImage(image);  // 이미지 저장 및 URL 생성
                    ReviewImage reviewImage = ReviewImage.builder()
                            .review(savedReview)
                            .imageUrl(imageUrl)
                            .build();
                    reviewImageRepository.save(reviewImage);
                } else {
                    log.warn("null이거나 빈 이미지가 발견되었습니다. 해당 이미지는 무시됩니다.");
                }
            }
        } else {
            log.warn("이미지가 null이거나 비어 있습니다. 이미지가 저장되지 않습니다.");
        }

        // 숙소의 평균 평점과 리뷰 수 업데이트
        updateAccommodationRatingsAndReviewCount(accommodation);

        log.info("리뷰 추가 완료: reviewId={}", savedReview.getId());
        return savedReview;
    }

    private void updateAccommodationRatingsAndReviewCount(Accommodation accommodation) {
        List<Review> reviews = reviewRepository.findByAccommodationId(accommodation.getId());
        if (reviews.isEmpty()) {
            accommodation.setAverageRating(0.0);
            accommodation.setReviewCount(0);
        } else {
            double averageRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            int reviewCount = reviews.size();

            accommodation.setAverageRating(averageRating);
            accommodation.setReviewCount(reviewCount);
        }
        accommodationRepository.save(accommodation);
    }


    private String saveImage(MultipartFile image) throws IOException {

        // 이미지 저장 디렉토리 설정
        Path uploadPath = Paths.get(uploadDir);
        log.info("업로드 디렉토리: {}", uploadPath);

        try {
            // 디렉토리 존재 여부 확인 및 생성
            if (!Files.exists(uploadPath)) {
                log.info("디렉토리가 존재하지 않으므로 생성합니다.");
                Files.createDirectories(uploadPath);
            }

            // 파일 이름 생성
            String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            log.info("파일 경로: {}", filePath);

            // 파일 저장
            Files.write(filePath, image.getBytes());
            log.info("파일이 성공적으로 저장되었습니다: {}", fileName);

            // 저장된 이미지의 URL 반환
            return "/upload/" + fileName;
        } catch (IOException e) {
            log.error("파일 저장 오류: {}", e.getMessage(), e);
            throw new IOException("이미지 저장 중 오류가 발생했습니다.", e);
        }
    }

    public List<Review> getReviewsByMember(Long memberId) {
        log.info("회원 ID를 기준으로 리뷰 조회: memberId={}", memberId);
        return reviewRepository.findAll().stream()
                .filter(review -> review.getMember().getId().equals(memberId))
                .collect(Collectors.toList());
    }

    public Page<Review> getReviewsByMemberWithPagination(Long memberId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository.findByMemberId(memberId, pageable);
    }


    public List<Review> getReviewsByAccommodation(Long accommodationId){
        return reviewRepository.findByAccommodationId(accommodationId);
    }

    public Review findReviewById(Long id) {
        Optional<Review> review = reviewRepository.findById(id);
        return review.orElse(null); // Review가 없을 경우 null 반환
    }
}
