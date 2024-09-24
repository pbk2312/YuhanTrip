package hello.yuhanTrip.service.discount;

import hello.yuhanTrip.dto.coupon.Coupon;
import hello.yuhanTrip.dto.coupon.DiscountType;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.dto.member.CouponDTO;

import java.util.List;

public interface CouponService {

    Coupon generateCoupon(Member member, DiscountType discountType, Double discountValue);

    // 쿠폰 ID로 쿠폰을 찾는 메서드 추가
    Coupon findCouponById(String couponCode,Member member);

    void deleteCoupon(String couponCode,Member member);



}
