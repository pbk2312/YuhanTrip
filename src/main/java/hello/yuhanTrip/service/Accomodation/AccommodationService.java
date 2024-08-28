package hello.yuhanTrip.service.Accomodation;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.AccommodationApplyStatus;
import hello.yuhanTrip.domain.AccommodationType;
import hello.yuhanTrip.domain.Room;
import hello.yuhanTrip.dto.AccommodationRegisterDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface AccommodationService {

    Accommodation registerAccommodation(Long memberId, AccommodationRegisterDTO dto) throws IOException;

    Accommodation getAccommodationInfo(Long id);

    Room getRoomInfo(Long id);

    Page<Accommodation> getAccommodations(int page, int size);

    Page<Accommodation> getAccommodationsByAreaCode(String areaCode, int page, int size);

    Page<Accommodation> getAvailableAccommodations(String areaCode, LocalDate checkInDate, LocalDate checkOutDate, int numGuests, int page, int size, String sortBy);


    Page<Accommodation> getAvailableAccommodationsSortedByRatingAndReview(Pageable pageable);

    List<Room> getAvailableRoomsByAccommodation(Long accommodationId, LocalDate checkInDate, LocalDate checkOutDate);



    Page<Accommodation> getAllAccommodationsOrderByPriceDesc(int page, int size);

    Page<Accommodation> getAllAccommodationsOrderByPriceAsc(int page, int size);

    Page<Accommodation> searchByTitle(String title, Pageable pageable);


    // 특정 숙소 유형(AccommodationType)으로 숙소를 필터링하는 메서드
    Page<Accommodation> getAccommodationsByStatusAndType(AccommodationApplyStatus status, AccommodationType type, int page, int size);

    // 지역 코드와 유형을 기반으로 숙소 리스트를 가져오는 메서드
    Page<Accommodation> getAccommodationsByAreaCodeAndType(String areaCode, AccommodationType type, int page, int size);

    // 유형, 지역 코드, 게스트 수, 체크인/체크아웃 날짜, 정렬 기준에 따라 필터링된 숙소를 가져오는 메서드
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



}
