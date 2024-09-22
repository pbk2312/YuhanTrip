package hello.yuhanTrip.service.discount;

import hello.yuhanTrip.domain.coupon.Coupon;
import hello.yuhanTrip.domain.coupon.DiscountType;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.dto.member.CouponDTO;
import hello.yuhanTrip.repository.CouponRepository;
import hello.yuhanTrip.repository.MemberRepository;
import hello.yuhanTrip.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final MemberRepository memberRepository;
    private final RedisService redisService;


    public boolean hasCoupon(Member member, DiscountType discountType) {
        return couponRepository.existsByMemberAndDiscountTypeAndUsed(member, discountType, false);
    }


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
        memberRepository.save(member); // 회원 정보는 DB에 저장

        // 쿠폰을 Redis에 저장 (만료 시간: 30일)
        Long expirationTime = 30L * 24 * 60 * 60 * 1000; // 30일을 밀리초로 변환
        redisService.saveCouponToRedis(coupon.getId(), coupon, expirationTime);

        log.info("쿠폰이 Redis에 저장되었습니다: {}", coupon);
        return coupon;
    }


    @Override
    public Coupon findCouponById(Long couponId) {
        return redisService.getCouponFromRedis(couponId);
    }

    @Transactional
    @Override
    public void deleteCoupon(Long couponId) {
        redisService.deleteCouponFromRedis(couponId);
    }


    @Transactional
    @Override
    public List<CouponDTO> getListCoupon(Member member) {
        List<CouponDTO> couponDTOList = new ArrayList<>();

        // Redis에서 쿠폰 정보를 가져오는 로직
        for (Coupon coupon : member.getCoupons()) {
            Coupon redisCoupon = redisService.getCouponFromRedis(coupon.getId());

            // Redis에 쿠폰이 없으면 기존 쿠폰 객체를 사용
            Coupon couponToUse = (redisCoupon != null) ? redisCoupon : coupon;

            // 쿠폰 정보를 CouponDTO로 변환
            CouponDTO dto = new CouponDTO();
            dto.setId(couponToUse.getId());
            dto.setCode(couponToUse.getCode());
            dto.setDiscountType(couponToUse.getDiscountType());
            dto.setDiscountValue(couponToUse.getDiscountValue());
            dto.setStartDate(couponToUse.getStartDate());
            dto.setEndDate(couponToUse.getEndDate());
            dto.setUsed(couponToUse.getUsed());
            dto.setMemberId(member.getId());

            couponDTOList.add(dto);
        }

        return couponDTOList;
    }

    // 랜덤 쿠폰 코드 생성
    private String generateCouponCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10); // 10자리 랜덤 코드
    }
}