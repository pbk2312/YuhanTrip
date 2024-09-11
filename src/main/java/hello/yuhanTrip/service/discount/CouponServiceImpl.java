package hello.yuhanTrip.service.discount;

import hello.yuhanTrip.domain.coupon.Coupon;
import hello.yuhanTrip.domain.coupon.DiscountType;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.repository.CouponRepository;
import hello.yuhanTrip.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public Coupon generateCoupon(Member member, DiscountType discountType, Double discountValue) {
        Coupon coupon = new Coupon();

        // 랜덤 쿠폰 코드 생성 및 중복 체크
        String couponCode;
        do {
            couponCode = generateCouponCode();
        } while (couponRepository.existsByCode(couponCode));
        coupon.setCode(couponCode);

        // 할인 유형과 할인 값을 설정
        coupon.setDiscountType(discountType);
        coupon.setDiscountValue(discountValue);

        // 쿠폰 유효 기간 설정 (현재 날짜로부터 30일)
        coupon.setStartDate(LocalDateTime.now());
        coupon.setEndDate(LocalDateTime.now().plusDays(30));
        coupon.setUsed(false); // 초기 발급 시 사용 여부는 false

        coupon.setMember(member); // 쿠폰과 회원 연결

        member.getCoupons().add(coupon);
        // 회원과 쿠폰 모두 저장
        log.info("쿠폰 저장: {}", coupon);
        memberRepository.save(member); // 회원 업데이트
        return couponRepository.save(coupon); // 쿠폰 저장
    }

    @Override
    public Coupon findCouponById(Long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
    }
    @Transactional
    @Override
    public void deleteCoupon(Long couponId) {
        // 쿠폰이 존재하는지 확인
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰이 존재하지 않아요"));

        // 쿠폰 삭제
        couponRepository.delete(coupon);
    }

    // 랜덤 쿠폰 코드 생성
    private String generateCouponCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10); // 10자리 랜덤 코드
    }
}