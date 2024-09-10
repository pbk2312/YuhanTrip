package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.domain.member.MemberRole;
import hello.yuhanTrip.domain.admin.RequestStatus;
import hello.yuhanTrip.domain.admin.RoleChangeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleChangeRequestRepository extends JpaRepository<RoleChangeRequest, Long> {

    // 중복 요청 여부 확인을 위한 메서드 추가
    boolean existsByMemberAndRequestedRoleAndStatus(Member member, MemberRole requestedRole, RequestStatus status);

    // RequestStatus가 PENDING인 요청들을 페이지 단위로 조회하는 메서드 추가
    Page<RoleChangeRequest> findByStatus(RequestStatus status, Pageable pageable);
}
