package hello.yuhanTrip.service.Accomodation;


import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.Like;
import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.repository.AccommodationRepository;
import hello.yuhanTrip.repository.LikeRepository;
import hello.yuhanTrip.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final AccommodationService accommodationService;
    private final MemberService memberService;

    public void likeAccommodation(String memberEmail, Long accommodationId) {
        Accommodation accommodation = accommodationService.getAccommodationInfo(accommodationId);

        Member member = memberService.findByEmail(memberEmail);

        // 이미 좋아요를 눌렀는지 확인
        Optional<Like> existingLike = likeRepository.findByMemberAndAccommodation(member, accommodation);

        if (existingLike.isPresent()) {
            // 이미 좋아요를 눌렀으면 좋아요 취소
            likeRepository.delete(existingLike.get());
        } else {
            // 좋아요 추가
            Like like = new Like();
            like.setMember(member);
            like.setAccommodation(accommodation);
            likeRepository.save(like);
        }
    }

    public long getLikeCount(Long accommodationId) {
        Accommodation accommodation = accommodationService.getAccommodationInfo(accommodationId);
        return likeRepository.countByAccommodation(accommodation);
    }




}
