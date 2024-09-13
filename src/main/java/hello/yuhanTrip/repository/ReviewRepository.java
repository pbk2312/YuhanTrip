package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.accommodation.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {


    Page<Review> findByMemberId(Long memberId, Pageable pageable);


    List<Review> findByAccommodationId(Long accommodationId);




}
