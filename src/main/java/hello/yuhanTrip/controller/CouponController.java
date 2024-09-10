package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.Coupon;
import hello.yuhanTrip.domain.DiscountType;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.service.discount.CouponService;
import hello.yuhanTrip.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Log4j2
public class CouponController {

    private final CouponService couponService;
    private final MemberService memberService;

    // 1. 고정 금액 할인 쿠폰 발급 API (예: 2000원 할인 쿠폰 발급)
    @PostMapping("/fixed")
    public ResponseEntity<Coupon> generateFixedAmountCoupon(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam Double discountAmount) {

        Member member = memberService.getUserDetails(accessToken);

        log.info("고정 금액 할인 쿠폰 발급");

        // FixedAmountDiscount 대신 DiscountType.FIXED 사용
        Coupon coupon = couponService.generateCoupon(member, DiscountType.FIXED, discountAmount);
        return ResponseEntity.ok(coupon);
    }

    // 2. 비율 할인 쿠폰 발급 API (예: 10% 할인 쿠폰 발급)
    @PostMapping("/percentage")
    public ResponseEntity<Coupon> generatePercentageCoupon(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam Double discountRate) {

        Member member = memberService.getUserDetails(accessToken);

        log.info("비율 할인 쿠폰 발급");

        // PercentageDiscount 대신 DiscountType.PERCENTAGE 사용
        Coupon coupon = couponService.generateCoupon(member, DiscountType.PERCENTAGE, discountRate);
        return ResponseEntity.ok(coupon);
    }
}