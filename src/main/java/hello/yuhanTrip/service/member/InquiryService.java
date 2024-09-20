package hello.yuhanTrip.service.member;

import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.dto.member.InquiryDTO;

public interface InquiryService {

    void saveInquiry(InquiryDTO inquiryDTO, Member member);
}
