package hello.yuhanTrip.service.Accomodation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hello.yuhanTrip.domain.*;
import hello.yuhanTrip.dto.AccommodationRegisterDTO;
import hello.yuhanTrip.repository.AccommodationRepository;
import hello.yuhanTrip.repository.RoomRepository;
import hello.yuhanTrip.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
@Log4j2
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {
    private final AccommodationRepository accommodationRepository;

    private final RoomRepository roomRepository;
    private final MemberService memberService;
    private final AccommodationFactory accommodationFactory; // AccommodationFactory 주입

    @JsonIgnore
    @Override
    public Accommodation registerAccommodation(Long memberId, AccommodationRegisterDTO dto) throws IOException {
        // 호스트 검증
        Member member = memberService.validateHost(memberId);

        Accommodation accommodation = accommodationFactory.createAccommodationFromDTO(dto, member);

        return accommodationRepository.save(accommodation);

    }

    /**
     * 숙소 정보를 가져옵니다.
     *
     * @param id 숙소 ID
     * @return 숙소 정보
     */

    @Override
    public Accommodation getAccommodationInfo(Long id) {
        return accommodationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 숙소입니다."));
    }

    /**
     * 객실 정보를 가져옵니다.
     *
     * @param id 객실 ID
     * @return 객실 정보
     */

    @Override

    public Room getRoomInfo(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("객실 정보 없음"));
    }


    /**
     * 승인된 숙소를 페이지네이션하여 반환합니다.
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이지네이션된 숙소 목록
     */

    @Override
    public Page<Accommodation> getAccommodations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findAllByStatus(AccommodationApplyStatus.APPROVED, pageable);
    }

    /**
     * 지역 코드에 따라 숙소를 페이지네이션하여 조회합니다.
     *
     * @param areaCode 지역 코드
     * @param page     페이지 번호
     * @param size     페이지 크기
     * @return 페이지네이션된 숙소 목록
     */

    @Override
    public Page<Accommodation> getAccommodationsByAreaCode(String areaCode, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findByAreacode(areaCode, pageable);
    }

    /**
     * 해당 날짜에 예약이 가능한 승인된 숙소를 보여줍니다.
     *
     * @param areaCode     지역 코드
     * @param checkInDate  체크인 날짜
     * @param checkOutDate 체크아웃 날짜
     * @param numGuests    인원 수
     * @param page         페이지 번호
     * @param size         페이지 크기
     * @param sortBy       정렬 기준 (예: "RATING", "PRICE_DESC", "PRICE_ASC")
     * @return 페이지네이션된 예약 가능한 숙소 목록
     */
    @Override
    public Page<Accommodation> getAvailableAccommodations(
            String areaCode, LocalDate checkInDate, LocalDate checkOutDate, int numGuests, int page, int size, String sortBy) {
        // 페이지 요청을 생성
        Pageable pageable = PageRequest.of(page, size);

        // 정렬 기준을 추가하여 메서드 호출
        return accommodationRepository.findAvailableAccommodations(
                AccommodationApplyStatus.APPROVED,
                areaCode,
                checkInDate,
                checkOutDate,
                numGuests,
                sortBy,
                pageable
        );
    }


    /**
     * 상태에 따라 숙소를 조회하고, 평점과 리뷰 수로 정렬하여 반환합니다.
     *
     * @param pageable 페이지 정보
     * @return 페이지네이션된 숙소 목록
     */

    @Override
    public Page<Accommodation> getAvailableAccommodationsSortedByRatingAndReview(Pageable pageable) {
        return accommodationRepository.findAllByStatusWithSorting(AccommodationApplyStatus.APPROVED, pageable);
    }

    /**
     * 특정 숙소의 예약 가능한 객실을 조회합니다.
     *
     * @param accommodationId 숙소 ID
     * @param checkInDate     체크인 날짜
     * @param checkOutDate    체크아웃 날짜
     * @return 예약 가능한 객실 목록
     */

    @Override
    public List<Room> getAvailableRoomsByAccommodation(Long accommodationId, LocalDate checkInDate, LocalDate checkOutDate) {
        List<ReservationStatus> excludedStatuses = Arrays.asList(
                ReservationStatus.CANCELLED,
                ReservationStatus.REJECTED
        );
        return roomRepository.findAvailableRoomsByAccommodation(
                accommodationId, checkInDate, checkOutDate, excludedStatuses
        );
    }


    /**
     * 전체 숙소 중 높은 가격순으로 조회합니다.
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이지네이션된 높은 가격순 숙소 목록
     */

    @Override
    public Page<Accommodation> getAllAccommodationsOrderByPriceDesc(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findAllByStatusOrderByAveragePriceDesc(
                AccommodationApplyStatus.APPROVED, pageable
        );
    }

    /**
     * 전체 숙소 중 낮은 가격순으로 조회합니다.
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이지네이션된 낮은 가격순 숙소 목록
     */

    @Override
    public Page<Accommodation> getAllAccommodationsOrderByPriceAsc(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findAllByStatusOrderByAveragePriceAsc(
                AccommodationApplyStatus.APPROVED, pageable
        );
    }

    /**
     * 숙소 제목으로 검색하여 결과를 반환합니다.
     *
     * @param title    숙소 제목
     * @param pageable 페이지 정보
     * @return 검색 결과 페이지
     */

    @Override
    public Page<Accommodation> searchByTitle(String title, Pageable pageable) {
        return accommodationRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    @Override
    public Page<Accommodation> getAvailableAccommodationsSearchByTitle(String title, String areaCode,
                                                                       LocalDate checkInDate, LocalDate checkOutDate, int numGuests,
                                                                       String sortBy, Pageable pageable) {
        return accommodationRepository.findByTitleWithFiltersAndSort(title, AccommodationApplyStatus.APPROVED, areaCode, checkInDate, checkOutDate, numGuests, sortBy, pageable);

    }



}