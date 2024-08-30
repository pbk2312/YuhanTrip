package hello.yuhanTrip.service.Accomodation;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.AccommodationType;
import hello.yuhanTrip.domain.Room;
import hello.yuhanTrip.dto.AccommodationRegisterDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface AccommodationService {

    // 숙소를 등록하는 메서드
    Accommodation registerAccommodation(Long memberId, AccommodationRegisterDTO dto) throws IOException;

    // 특정 ID를 가진 숙소 정보를 반환하는 메서드
    Accommodation getAccommodationInfo(Long id);

    // 특정 ID를 가진 방 정보를 반환하는 메서드
    Room getRoomInfo(Long id);

    // 페이지 번호와 사이즈를 기준으로 숙소 목록을 반환하는 메서드
    Page<Accommodation> getAccommodations(int page, int size);

    // 지역 코드에 따라 숙소 목록을 반환하는 메서드
    Page<Accommodation> getAccommodationsByAreaCode(String areaCode, int page, int size);

    // 평점 및 리뷰 기준으로 정렬된 사용 가능한 숙소 목록을 반환하는 메서드
    Page<Accommodation> getAvailableAccommodationsSortedByRatingAndReview(Pageable pageable);

    // 특정 숙소의 사용 가능한 방을 체크인 및 체크아웃 날짜를 기준으로 반환하는 메서드
    List<Room> getAvailableRoomsByAccommodation(Long accommodationId, LocalDate checkInDate, LocalDate checkOutDate);

    // 가격을 기준으로 내림차순 정렬된 모든 숙소 목록을 반환하는 메서드
    Page<Accommodation> getAllAccommodationsOrderByPriceDesc(int page, int size);

    // 가격을 기준으로 오름차순 정렬된 모든 숙소 목록을 반환하는 메서드
    Page<Accommodation> getAllAccommodationsOrderByPriceAsc(int page, int size);

    // 제목을 기준으로 숙소를 검색하여 결과를 반환하는 메서드
    Page<Accommodation> searchByTitle(String title, Pageable pageable);

    // 유형, 지역 코드, 체크인/체크아웃 날짜, 게스트 수, 정렬 기준에 따라 필터링된 숙소 목록을 반환하는 메서드
    Page<Accommodation> findAvailableAccommodationsByType(
            AccommodationType type,
            String areaCode,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int numGuests,
            String sortBy,
            int page,
            int size
    );

    // 대기 중인 숙소 목록을 반환하는 메서드
    Page<Accommodation> getPendingAccommodations(Pageable pageable);

    // 특정 숙소를 승인하는 메서드
    void approveAccommodation(Long id);

    // 유형에 따라 평점 및 리뷰 기준으로 정렬된 숙소 목록을 반환하는 메서드
    Page<Accommodation> getAccommodationsByTypeSortedByRatingAndReview(AccommodationType type, Pageable pageable);

    // 유형에 따라 가격을 기준으로 내림차순 정렬된 숙소 목록을 반환하는 메서드
    Page<Accommodation> getAccommodationsByTypeOrderByPriceDesc(AccommodationType type, int page, int size);

    // 유형에 따라 가격을 기준으로 오름차순 정렬된 숙소 목록을 반환하는 메서드
    Page<Accommodation> getAccommodationsByTypeOrderByPriceAsc(AccommodationType type, int page, int size);
}