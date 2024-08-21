package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.Accommodation;

import hello.yuhanTrip.domain.AccommodationApplyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;


public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {


    @Query("SELECT a FROM Accommodation a WHERE a.status = :status")
    Page<Accommodation> findAllByStatus(@Param("status") AccommodationApplyStatus status, Pageable pageable);


    @Query("SELECT a FROM Accommodation a WHERE a.status = :status " +
            "ORDER BY a.averageRating DESC, a.reviewCount DESC")
    Page<Accommodation> findAllByStatusWithSorting(
            @Param("status") AccommodationApplyStatus status,
            Pageable pageable
    );




    // 지역 코드로 숙소 리스트를 가져오는 쿼리 메서드
    Page<Accommodation> findByAreacode(String areacode, Pageable pageable);


    @Query("SELECT DISTINCT a FROM Accommodation a " +
            "JOIN a.rooms r " +
            "WHERE a.status = :status " + // 추가된 조건
            "AND (:areaCode IS NULL OR a.areacode = :areaCode) " +
            "AND r.maxOccupancy >= :numGuests " +
            "AND EXISTS (" +
            "  SELECT 1 FROM Room r2 " +
            "  WHERE r2.accommodation = a " +
            "  AND r2.maxOccupancy >= :numGuests " +
            "  AND NOT EXISTS (" +
            "    SELECT 1 FROM Reservation res " +
            "    WHERE res.room = r2 " +
            "    AND res.checkInDate < :checkOutDate " +
            "    AND res.checkOutDate > :checkInDate" +
            "  )" +
            ")")
    Page<Accommodation> findAvailableAccommodations(
            @Param("status") AccommodationApplyStatus status, // 추가된 파라미터
            @Param("areaCode") String areaCode,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("numGuests") int numGuests,
            Pageable pageable
    );

    @Query("SELECT DISTINCT a FROM Accommodation a " +
            "JOIN a.rooms r " +
            "WHERE a.status = :status " +
            "AND (:areaCode IS NULL OR a.areacode = :areaCode) " +
            "AND r.maxOccupancy >= :numGuests " +
            "AND EXISTS (" +
            "  SELECT 1 FROM Room r2 " +
            "  WHERE r2.accommodation = a " +
            "  AND r2.maxOccupancy >= :numGuests " +
            "  AND NOT EXISTS (" +
            "    SELECT 1 FROM Reservation res " +
            "    WHERE res.room = r2 " +
            "    AND res.checkInDate < :checkOutDate " +
            "    AND res.checkOutDate > :checkInDate" +
            "  )" +
            ") " +
            "ORDER BY a.averageRating DESC, a.reviewCount DESC")
    Page<Accommodation> findAvailableAccommodationsByAverageRating(
            @Param("status") AccommodationApplyStatus status,
            @Param("areaCode") String areaCode,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("numGuests") int numGuests,
            Pageable pageable
    );






}