package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.MemberLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberLikeRepository extends JpaRepository<MemberLike,Long> {


    Optional<MemberLike> findByMemberIdAndAccommodationId(Long memberId, Long accommodationId);
    void deleteByMemberIdAndAccommodationId(Long memberId, Long accommodationId);


    Page<MemberLike> findByMemberId(Long memberId, Pageable pageable);

}
