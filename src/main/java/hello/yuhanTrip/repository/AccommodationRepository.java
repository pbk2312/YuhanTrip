package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.Accommodation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AccommodationRepository extends JpaRepository<Accommodation,Long> {

    // 지역 코드로 숙소 리스트를 가져오는 쿼리 메서드
    Page<Accommodation> findByAreacode(String areacode, Pageable pageable);


}
