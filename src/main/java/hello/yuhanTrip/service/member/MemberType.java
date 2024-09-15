package hello.yuhanTrip.service.member;

import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.dto.register.MemberRequestDTO;

public interface MemberType {
    Member register(MemberRequestDTO memberRequestDTO);
}
