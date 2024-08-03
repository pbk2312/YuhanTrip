package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.Accommodation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccommodationRepository extends JpaRepository<Accommodation,Long> {


    // 지역 코드를 기반으로 숙소를 필터링하는 메서드
    Page<Accommodation> findByAreacode(String areacode, Pageable pageable);

}
