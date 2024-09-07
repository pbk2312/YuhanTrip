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
import org.springframework.transaction.annotation.Transactional;

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
    private final AccommodationFactory accommodationFactory;

    @JsonIgnore
    @Override
    public Accommodation registerAccommodation(String accessToken, AccommodationRegisterDTO dto) throws IOException {

        // 호스트 검증
        Member member = memberService.validateHost(accessToken);

        Accommodation accommodation = accommodationFactory.createAccommodationFromDTO(dto, member);

        return accommodationRepository.save(accommodation);

    }


    @Override
    public Accommodation getAccommodationInfo(Long id) {
        return accommodationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 숙소입니다."));
    }


    @Override
    public Room getRoomInfo(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("객실 정보 없음"));
    }



    @Override
    public Page<Accommodation> getAccommodations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findAllByStatus(AccommodationApplyStatus.APPROVED, pageable);
    }


    @Override
    public Page<Accommodation> getAccommodationsByAreaCode(String areaCode, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findByAreacode(areaCode, pageable);
    }



    @Override
    public Page<Accommodation> getAvailableAccommodationsSortedByRatingAndReview(Pageable pageable) {
        return accommodationRepository.findAllByStatusWithSorting(AccommodationApplyStatus.APPROVED, pageable);
    }



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




    @Override
    public Page<Accommodation> getAllAccommodationsOrderByPriceDesc(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findAllByStatusOrderByAveragePriceDesc(
                AccommodationApplyStatus.APPROVED, pageable
        );
    }



    @Override
    public Page<Accommodation> getAllAccommodationsOrderByPriceAsc(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findAllByStatusOrderByAveragePriceAsc(
                AccommodationApplyStatus.APPROVED, pageable
        );
    }


    @Override
    public Page<Accommodation> searchByTitle(String title, Pageable pageable) {
        return accommodationRepository.findByTitleContainingIgnoreCase(title, pageable);
    }



    @Override
    public Page<Accommodation> findAvailableAccommodationsByType(
            AccommodationType type,
            String areaCode,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int numGuests,
            String sortBy,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findAvailableAccommodationsByType(
                AccommodationApplyStatus.APPROVED,
                type,
                areaCode,
                checkInDate,
                checkOutDate,
                numGuests,
                sortBy,
                pageable
        );
    }


    public Page<Accommodation> getPendingAccommodations(Pageable pageable) {
        return accommodationRepository.findAllByStatus(AccommodationApplyStatus.PENDING, pageable);
    }

    @Transactional
    public void approveAccommodation(Long id) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("숙소를 찾을 수 없습니다."));
        accommodation.setStatus(AccommodationApplyStatus.APPROVED);
        accommodationRepository.save(accommodation);
    }

    public Page<Accommodation> getAccommodationsByTypeSortedByRatingAndReview(AccommodationType type, Pageable pageable) {
        // `type` 필터링과 동시에 평균 평점으로 정렬된 결과 반환
        return accommodationRepository.findByStatusAndTypeOrderByAverageRatingDesc(AccommodationApplyStatus.APPROVED,type, pageable);
    }

    public Page<Accommodation> getAccommodationsByTypeOrderByPriceDesc(AccommodationType type, int page, int size) {
        // `type` 필터링과 동시에 가격 내림차순 정렬된 결과 반환
        return accommodationRepository.findByStatusAndTypeOrderByPriceDesc(AccommodationApplyStatus.APPROVED,type, PageRequest.of(page, size));
    }

    public Page<Accommodation> getAccommodationsByTypeOrderByPriceAsc(AccommodationType type, int page, int size) {
        // `type` 필터링과 동시에 가격 오름차순 정렬된 결과 반환
        return accommodationRepository.findByStatusAndTypeOrderByPriceAsc(AccommodationApplyStatus.APPROVED,type, PageRequest.of(page, size));
    }
}