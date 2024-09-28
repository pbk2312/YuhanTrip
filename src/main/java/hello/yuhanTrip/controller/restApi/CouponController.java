package hello.yuhanTrip.controller.restApi;

import hello.yuhanTrip.dto.coupon.Coupon;
import hello.yuhanTrip.dto.coupon.DiscountType;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.dto.ResponseDTO;
import hello.yuhanTrip.exception.UnauthorizedException;
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

    // 액세스 토큰 검증 및 회원 조회
    private Member validateAndGetMember(String accessToken) {
        if (accessToken == null) {
            throw new UnauthorizedException(LOGIN_REQUIRED_MESSAGE);
        }

        return memberService.getUserDetails(accessToken);
    }

    // 쿠폰 발급 공통 메서드
    private ResponseEntity<ResponseDTO<?>> generateCoupon(Member member, DiscountType discountType, Double discountValue) {
        if (couponService.hasCoupon(member.getId())) {
            ResponseDTO<String> response = new ResponseDTO<>("해당 계정은 이미 쿠폰을 받았습니다.", null);
            return ResponseEntity.badRequest().body(response);
        }
        Coupon coupon = couponService.generateCoupon(member, discountType, discountValue);
        log.info("{} 할인 쿠폰 발급 - 회원 ID: {}", discountType, member.getId());

        ResponseDTO<Coupon> response = new ResponseDTO<>("쿠폰 발급이 완료되었습니다.", coupon);
        return ResponseEntity.ok(response);
    }


    // 쿠폰 발급 API (고정 금액/비율 공통 처리)
    @PostMapping("/{type}")
    public ResponseEntity<ResponseDTO<?>> generateCoupon(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @PathVariable String type,
            @RequestParam(required = false) Double discountAmount,
            @RequestParam(required = false) Double discountRate) {

        Member member = validateAndGetMember(accessToken);

        DiscountType discountType = "fixed".equalsIgnoreCase(type) ? DiscountType.FIXED : DiscountType.PERCENTAGE;
        Double discountValue = discountType == DiscountType.FIXED ? discountAmount : discountRate;

        return generateCoupon(member, discountType, discountValue);
    }
}