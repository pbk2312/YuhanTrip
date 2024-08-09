package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.Like;
import hello.yuhanTrip.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like,Long> {

    Optional<Like> findByMemberAndAccommodation(Member member, Accommodation accommodation);
    long countByAccommodation(Accommodation accommodation);
}
