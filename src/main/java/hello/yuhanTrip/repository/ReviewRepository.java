package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review,Long> {

}
