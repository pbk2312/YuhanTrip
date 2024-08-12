package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review,Long> {


    Page<Review> findByMemberId(Long memberId, Pageable pageable);
}
