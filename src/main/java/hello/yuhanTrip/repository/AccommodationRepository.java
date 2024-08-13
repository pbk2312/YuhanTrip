package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.Accommodation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;


public interface AccommodationRepository extends JpaRepository<Accommodation,Long> {

    // 지역 코드로 숙소 리스트를 가져오는 쿼리 메서드
    Page<Accommodation> findByAreacode(String areacode, Pageable pageable);




    @Query("SELECT DISTINCT a FROM Accommodation a " +
            "JOIN a.rooms r " +
            "WHERE (:areaCode IS NULL OR a.areacode = :areaCode) " +
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
            @Param("areaCode") String areaCode,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("numGuests") int numGuests,
            Pageable pageable
    );

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.accommodation.id = :accommodationId")
    Double findAverageRatingByAccommodationId(@Param("accommodationId") Long accommodationId);


}