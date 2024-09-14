package hello.yuhanTrip.mapper;

import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.dto.member.MemberDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MemberMapper {

    MemberMapper INSTANCE = Mappers.getMapper(MemberMapper.class);

    MemberDTO toMemberDTO(Member member);

}