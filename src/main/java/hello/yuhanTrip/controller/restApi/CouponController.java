package hello.yuhanTrip.controller.restApi;

import hello.yuhanTrip.dto.coupon.Coupon;
import hello.yuhanTrip.dto.coupon.DiscountType;
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

    private static final String LOGIN_REQUIRED_MESSAGE = "로그인이 필요합니다.";
    private static final String COUPON_ALREADY_ISSUED_MESSAGE = "이미 발급된 쿠폰이 있습니다.";

    // 액세스 토큰 검증 및 회원 조회 공통 메서드
    private ResponseEntity<?> validateAndGetMember(String accessToken) {
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(LOGIN_REQUIRED_MESSAGE);
        }

        Member member = memberService.getUserDetails(accessToken);
        return ResponseEntity.ok(member);
    }

    // 고정 금액 할인 쿠폰 발급 API
    @PostMapping("/fixed")
    public ResponseEntity<?> generateFixedAmountCoupon(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam Double discountAmount) {

        ResponseEntity<?> memberValidationResult = validateAndGetMember(accessToken);
        if (memberValidationResult.getStatusCode() != HttpStatus.OK) {
            return memberValidationResult;
        }
        Member member = (Member) memberValidationResult.getBody();



        log.info("고정 금액 할인 쿠폰 발급 - 회원 ID: {}", member.getId());

        Coupon coupon = couponService.generateCoupon(member, DiscountType.FIXED, discountAmount);
        return ResponseEntity.ok(coupon);
    }

    // 비율 할인 쿠폰 발급 API
    @PostMapping("/percentage")
    public ResponseEntity<?> generatePercentageCoupon(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @RequestParam Double discountRate) {

        ResponseEntity<?> memberValidationResult = validateAndGetMember(accessToken);
        if (memberValidationResult.getStatusCode() != HttpStatus.OK) {
            return memberValidationResult;
        }
        Member member = (Member) memberValidationResult.getBody();



        log.info("비율 할인 쿠폰 발급 - 회원 ID: {}", member.getId());

        Coupon coupon = couponService.generateCoupon(member, DiscountType.PERCENTAGE, discountRate);
        return ResponseEntity.ok(coupon);
    }
}