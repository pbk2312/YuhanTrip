package hello.yuhanTrip.service.member;

import hello.yuhanTrip.domain.member.Inquiry;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.dto.member.InquiryDTO;
import hello.yuhanTrip.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;


@RequiredArgsConstructor
@Service
public class InquiryServiceImpl implements InquiryService{

    private final InquiryRepository inquiryRepository;
    @Override
    public void saveInquiry(InquiryDTO inquiryDTO, Member member) {
        Inquiry inquiry = Inquiry.builder()
                .crateAt(LocalDate.now())
                .member(member)
                .message(inquiryDTO.getMessage())
                .subject(inquiryDTO.getSubject())
                .build();
        inquiryRepository.save(inquiry);
    }
}
