package hello.yuhanTrip.service.discount;

import hello.yuhanTrip.dto.coupon.Coupon;
import hello.yuhanTrip.dto.coupon.DiscountType;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.repository.MemberRepository;
import hello.yuhanTrip.service.RedisService;
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

    private final MemberRepository memberRepository;
    private final RedisService redisService;


    @Override
    @Transactional
    public Coupon generateCoupon(Member member, DiscountType discountType, Double discountValue) {
        Coupon coupon = new Coupon();

        // 랜덤 쿠폰 코드 생성 및 중복 체크
        String couponCode;
        couponCode = generateCouponCode();
        coupon.setCode(couponCode);

        // 할인 유형과 할인 값을 설정
        coupon.setDiscountType(discountType);
        coupon.setDiscountValue(discountValue);

        // 쿠폰 유효 기간 설정 (현재 날짜로부터 30일)
        coupon.setStartDate(LocalDateTime.now());
        coupon.setEndDate(LocalDateTime.now().plusDays(30));
        coupon.setUsed(false); // 초기 발급 시 사용 여부는 false

        // 쿠폰을 Redis에 저장 (만료 시간: 30일)
        Long expirationTime = 30L * 24 * 60 * 60 * 1000; // 30일을 밀리초로 변환
        redisService.saveCouponToRedis(member, coupon, expirationTime);

        log.info("쿠폰이 Redis에 저장되었습니다: {}", coupon);
        return coupon;
    }


    @Override
    public Coupon findCouponById(String couponCode,Member member) {
        return redisService.getFromRedis(couponCode,member);
    }

    @Transactional
    @Override
    public void deleteCoupon(String couponCode,Member member) {
        redisService.deleteCouponFromRedis(couponCode,member);
    }


    // 랜덤 쿠폰 코드 생성
    private String generateCouponCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10); // 10자리 랜덤 코드
    }
}