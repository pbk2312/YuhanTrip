package hello.yuhanTrip.service;


import hello.yuhanTrip.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ReviewService {

    /**
     * 리뷰와 관련된 이미지를 추가합니다.
     *
     * @param accommodationId 숙소 ID
     * @param memberEmail      회원 이메일
     * @param reservationId    예약 ID
     * @param content          리뷰 내용
     * @param rating           평점
     * @param images           이미지 파일 리스트
     * @return 저장된 리뷰 객체
     * @throws java.io.IOException 파일 저장 중 오류 발생 시
     */
    Review addReviewWithImages(Long accommodationId, String memberEmail, Long reservationId, String content, int rating, List<MultipartFile> images) throws IOException;


    /**
     * 페이지네이션을 적용하여 회원 ID로 작성된 리뷰를 조회합니다.
     *
     * @param memberId 회원 ID
     * @param page     페이지 번호
     * @param size     페이지 크기
     * @return 페이지네이션된 리뷰 목록
     */
    Page<Review> getReviewsByMemberWithPagination(Long memberId, int page, int size);

    /**
     * 숙소 ID로 작성된 리뷰를 조회합니다.
     *
     * @param accommodationId 숙소 ID
     * @return 숙소에 대한 리뷰 목록
     */
    List<Review> getReviewsByAccommodation(Long accommodationId);

    /**
     * 리뷰 ID로 리뷰를 조회합니다.
     *
     * @param id 리뷰 ID
     * @return 리뷰 객체
     */
    Review findReviewById(Long id);

}

