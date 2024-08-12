package hello.yuhanTrip.service;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.domain.Reservation;
import hello.yuhanTrip.domain.Review;
import hello.yuhanTrip.domain.ReviewImage;
import hello.yuhanTrip.repository.ReviewRepository;
import hello.yuhanTrip.repository.ReviewImageRepository;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
import hello.yuhanTrip.service.Accomodation.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewImageRepository reviewImageRepository;

    @Mock
    private AccommodationService accommodationService;

    @Mock
    private MemberService memberService;

    @Mock
    private ReservationService reservationService;


    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Reflection을 사용하여 private 필드에 접근
        Field uploadDirField = ReviewService.class.getDeclaredField("uploadDir");
        uploadDirField.setAccessible(true);
        uploadDirField.set(reviewService, "uploads");  // 필드 값 설정
    }
    @Test
    void testAddReviewWithImages_Success() throws IOException {
        // Given
        Long accommodationId = 1L;
        String memberEmail = "test@example.com";
        Long reservationId = 1L;
        String content = "Great stay!";
        int rating = 5;
        List<MultipartFile> images = new ArrayList<>();
        images.add(new MockMultipartFile("image", "image.jpg", "image/jpeg", "test image content".getBytes()));

        Accommodation accommodation = mock(Accommodation.class);
        Member member = mock(Member.class);
        Reservation reservation = mock(Reservation.class);

        when(accommodationService.getAccommodationInfo(accommodationId)).thenReturn(accommodation);
        when(memberService.findByEmail(memberEmail)).thenReturn(member);
        when(reservationService.findReservation(reservationId)).thenReturn(reservation);
        when(reservation.getMember()).thenReturn(member);
        when(reservation.getAccommodationId()).thenReturn(accommodationId);
        when(reservation.getReview()).thenReturn(null);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Review review = reviewService.addReviewWithImages(accommodationId, memberEmail, reservationId, content, rating, images);

        // Then
        assertNotNull(review);
        assertEquals(content, review.getContent());
        assertEquals(rating, review.getRating());
        assertEquals(accommodation, review.getAccommodation());
        assertEquals(member, review.getMember());
        verify(reviewRepository, times(1)).save(review);
        verify(reviewImageRepository, times(1)).save(any(ReviewImage.class));
    }

    @Test
    void testAddReviewWithImages_ThrowsIllegalStateException_WhenReviewAlreadyExists() {
        // Given
        Long accommodationId = 1L;
        String memberEmail = "test@example.com";
        Long reservationId = 1L;
        String content = "Great stay!";
        int rating = 5;
        List<MultipartFile> images = new ArrayList<>();

        Accommodation accommodation = mock(Accommodation.class);
        Member member = mock(Member.class);
        Reservation reservation = mock(Reservation.class);
        Review existingReview = mock(Review.class);

        when(accommodationService.getAccommodationInfo(accommodationId)).thenReturn(accommodation);
        when(memberService.findByEmail(memberEmail)).thenReturn(member);
        when(reservationService.findReservation(reservationId)).thenReturn(reservation);
        when(reservation.getMember()).thenReturn(member);
        when(reservation.getAccommodationId()).thenReturn(accommodationId);
        when(reservation.getReview()).thenReturn(existingReview);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                reviewService.addReviewWithImages(accommodationId, memberEmail, reservationId, content, rating, images)
        );

        assertEquals("이 예약에 대해서는 이미 리뷰가 작성되었습니다.", exception.getMessage());
    }

    @Test
    void testGetReviewsByMember() {
        // Given
        Long memberId = 1L;
        Member member = mock(Member.class);
        Review review1 = mock(Review.class);
        Review review2 = mock(Review.class);

        when(member.getId()).thenReturn(memberId);
        when(review1.getMember()).thenReturn(member);
        when(review2.getMember()).thenReturn(member);
        when(reviewRepository.findAll()).thenReturn(List.of(review1, review2));

        // When
        List<Review> reviews = reviewService.getReviewsByMember(memberId);

        // Then
        assertEquals(2, reviews.size());
        assertTrue(reviews.contains(review1));
        assertTrue(reviews.contains(review2));
    }
}