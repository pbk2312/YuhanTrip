package hello.yuhanTrip.mapper;

import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.dto.member.MemberDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MemberMapper {

    MemberMapper INSTANCE = Mappers.getMapper(MemberMapper.class);

    @Mapping(target = "accommodations", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "memberLikes", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "roleChangeRequests", ignore = true)
    @Mapping(target = "coupons", ignore = true)
    MemberDTO toMemberDTO(Member member);

    @Mapping(target = "accommodations", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "memberLikes", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "roleChangeRequests", ignore = true)
    @Mapping(target = "coupons", ignore = true)
    Member toMember(MemberDTO memberDTO);
}