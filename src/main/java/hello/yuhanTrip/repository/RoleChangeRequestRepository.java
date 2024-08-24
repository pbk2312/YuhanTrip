package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.domain.MemberRole;
import hello.yuhanTrip.domain.admin.RequestStatus;
import hello.yuhanTrip.domain.admin.RoleChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleChangeRequestRepository extends JpaRepository<RoleChangeRequest, Long> {

    // 중복 요청 여부 확인을 위한 메서드 추가
    boolean existsByMemberAndRequestedRoleAndStatus(Member member, MemberRole requestedRole, RequestStatus status);
}
