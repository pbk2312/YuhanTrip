package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.AccommodationApplyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

    // 전체 숙소 보여주기
    @Query("SELECT a FROM Accommodation a WHERE a.status = :status")
    Page<Accommodation> findAllByStatus(@Param("status") AccommodationApplyStatus status, Pageable pageable);

    // 평점순 정렬
    @Query("SELECT a FROM Accommodation a WHERE a.status = :status " +
            "ORDER BY a.averageRating DESC, a.reviewCount DESC")
    Page<Accommodation> findAllByStatusWithSorting(@Param("status") AccommodationApplyStatus status, Pageable pageable);

    // 지역 코드로 숙소 리스트를 가져오는 쿼리 메서드
    Page<Accommodation> findByAreacode(String areacode, Pageable pageable);

    @Query("SELECT DISTINCT a FROM Accommodation a " +
            "JOIN a.rooms r " +
            "WHERE a.status = :status " +
            "AND (:areaCode IS NULL OR a.areacode = :areaCode) " +
            "AND r.maxOccupancy >= :numGuests " +
            "AND NOT EXISTS (" +
            "    SELECT 1 FROM Reservation res " +
            "    WHERE res.room IN (SELECT r2 FROM Room r2 WHERE r2.accommodation = a) " +
            "    AND res.checkInDate < :checkOutDate " +
            "    AND res.checkOutDate > :checkInDate" +
            ") " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'RATING' THEN a.averageRating END DESC, " +
            "CASE WHEN :sortBy = 'RATING' THEN a.reviewCount END DESC, " +
            "CASE WHEN :sortBy = 'PRICE_DESC' THEN a.averagePrice END DESC, " +
            "CASE WHEN :sortBy = 'PRICE_ASC' THEN a.averagePrice END ASC")
    Page<Accommodation> findAvailableAccommodations(
            @Param("status") AccommodationApplyStatus status,
            @Param("areaCode") String areaCode,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("numGuests") int numGuests,
            @Param("sortBy") String sortBy,
            Pageable pageable
    );

    // 가격 높은 순
    @Query("SELECT a FROM Accommodation a WHERE a.status = :status " +
            "ORDER BY a.averagePrice DESC")
    Page<Accommodation> findAllByStatusOrderByAveragePriceDesc(
            @Param("status") AccommodationApplyStatus status,
            Pageable pageable
    );

    // 가격 낮은 순
    @Query("SELECT a FROM Accommodation a WHERE a.status = :status " +
            "ORDER BY a.averagePrice ASC")
    Page<Accommodation> findAllByStatusOrderByAveragePriceAsc(
            @Param("status") AccommodationApplyStatus status,
            Pageable pageable
    );

    // Title로 검색 결과를 페이지네이션하여 반환
    Page<Accommodation> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("SELECT a FROM Accommodation a " +
            "JOIN a.rooms r " +
            "WHERE a.title LIKE %:title% " +
            "AND a.status = :status " +
            "AND (:areaCode IS NULL OR a.areacode = :areaCode) " +
            "AND r.maxOccupancy >= :numGuests " +
            "AND NOT EXISTS (" +
            "    SELECT 1 FROM Reservation res " +
            "    WHERE res.room IN (SELECT r2 FROM Room r2 WHERE r2.accommodation = a) " +
            "    AND res.checkInDate < :checkOutDate " +
            "    AND res.checkOutDate > :checkInDate" +
            ") " +
            "ORDER BY " +
            "  CASE WHEN :sortBy = 'RATING' THEN a.averageRating END DESC, " +
            "  CASE WHEN :sortBy = 'RATING' THEN a.reviewCount END DESC, " +
            "  CASE WHEN :sortBy = 'PRICE_DESC' THEN a.averagePrice END DESC, " +
            "  CASE WHEN :sortBy = 'PRICE_ASC' THEN a.averagePrice END ASC")
    Page<Accommodation> findByTitleWithFiltersAndSort(
            @Param("title") String title,
            @Param("status") AccommodationApplyStatus status,
            @Param("areaCode") String areaCode,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("numGuests") int numGuests,
            @Param("sortBy") String sortBy,
            Pageable pageable
    );

}
