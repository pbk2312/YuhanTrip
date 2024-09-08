package hello.yuhanTrip.service.discount;

import hello.yuhanTrip.domain.Coupon;
import hello.yuhanTrip.domain.DiscountType;
import hello.yuhanTrip.domain.Member;

public interface CouponService {

    Coupon generateCoupon(Member member, DiscountType discountType, Double discountValue);

    // 쿠폰 ID로 쿠폰을 찾는 메서드 추가
    Coupon findCouponById(Long couponId);

    void deleteCoupon(Long couponId); // 쿠폰 삭제 메서드 추가
}
