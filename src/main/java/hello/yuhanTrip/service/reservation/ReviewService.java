package hello.yuhanTrip.service.reservation;

import hello.yuhanTrip.domain.accommodation.Review;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ReviewService {

    // 숙소에 대한 리뷰를 추가하고 이미지도 함께 업로드하는 메서드
    Review addReviewWithImages(
            Long accommodationId,              // 숙소 ID
            String memberEmail,                // 회원 이메일
            Long reservationId,                // 예약 ID
            String content,                    // 리뷰 내용
            int rating,                        // 리뷰 평점
            List<MultipartFile> images         // 업로드할 이미지 목록
    ) throws IOException;                   // 이미지 업로드 시 발생할 수 있는 예외 처리

    // 특정 회원이 작성한 리뷰를 페이지네이션하여 가져오는 메서드
    Page<Review> getReviewsByMemberWithPagination(
            Long memberId,    // 회원 ID
            int page,         // 페이지 번호
            int size          // 페이지당 리뷰 수
    );

    // 특정 숙소에 대한 모든 리뷰를 가져오는 메서드
    List<Review> getReviewsByAccommodation(
            Long accommodationId // 숙소 ID
    );

    // 리뷰 ID로 리뷰를 찾아 반환하는 메서드
    Review findReviewById(
            Long id // 리뷰 ID
    );
}