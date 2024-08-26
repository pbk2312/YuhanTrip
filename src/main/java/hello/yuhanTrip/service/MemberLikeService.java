package hello.yuhanTrip.service;


import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.domain.MemberLike;
import hello.yuhanTrip.repository.MemberLikeRepository;
import hello.yuhanTrip.service.Accomodation.AccommodationServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class MemberLikeService {

    private final AccommodationServiceImpl accommodationService;
    private final MemberLikeRepository memberLikeRepository;

    @Transactional
    public void addLike(Member member, Long accommodationId) {

        Accommodation accommodation = accommodationService.getAccommodationInfo(accommodationId);

            MemberLike memberLike = new MemberLike();
            memberLike.setMember(member);
            memberLike.setAccommodation(accommodation);
            memberLikeRepository.save(memberLike);
        }

    @Transactional
    public void removeLike(Member member, Long accommodationId) {
        memberLikeRepository.deleteByMemberIdAndAccommodationId(member.getId(), accommodationId);
    }

    @Transactional
    public boolean isLiked(Member member, Long accommodationId) {
        return memberLikeRepository.findByMemberIdAndAccommodationId(member.getId(), accommodationId).isPresent();
    }

    @Transactional(readOnly = true)
    public Page<Accommodation> getLikesByMember(Member member, Pageable pageable) {
        return memberLikeRepository.findByMemberId(member.getId(), pageable)
                .map(MemberLike::getAccommodation);
    }

}
