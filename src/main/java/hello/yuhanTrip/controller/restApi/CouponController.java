package hello.yuhanTrip.controller.restApi;

import hello.yuhanTrip.domain.coupon.Coupon;
import hello.yuhanTrip.domain.coupon.DiscountType;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.service.discount.CouponService;
import hello.yuhanTrip.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> generateFixedAmountCoupon(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam Double discountAmount) {

        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        Member member = memberService.getUserDetails(accessToken);

        // 이미 발급된 쿠폰 확인
        if (couponService.hasCoupon(member, DiscountType.FIXED)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 발급된 쿠폰이 있습니다.");
        }

        log.info("고정 금액 할인 쿠폰 발급");

        // FixedAmountDiscount 대신 DiscountType.FIXED 사용
        Coupon coupon = couponService.generateCoupon(member, DiscountType.FIXED, discountAmount);
        return ResponseEntity.ok(coupon);
    }

    // 비율 할인 쿠폰 발급 API
    @PostMapping("/percentage")
    public ResponseEntity<?> generatePercentageCoupon(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam Double discountRate) {

        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        Member member = memberService.getUserDetails(accessToken);

        // 이미 발급된 쿠폰 확인
        if (couponService.hasCoupon(member, DiscountType.PERCENTAGE)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 발급된 쿠폰이 있습니다.");
        }

        log.info("비율 할인 쿠폰 발급");

        // PercentageDiscount 대신 DiscountType.PERCENTAGE 사용
        Coupon coupon = couponService.generateCoupon(member, DiscountType.PERCENTAGE, discountRate);
        return ResponseEntity.ok(coupon);
    }
}