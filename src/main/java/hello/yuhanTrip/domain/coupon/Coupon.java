package hello.yuhanTrip.domain.coupon;

import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.service.discount.DiscountStrategy;
import hello.yuhanTrip.service.discount.FixedAmountDiscount;
import hello.yuhanTrip.service.discount.PercentageDiscount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Log4j2
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;            // 쿠폰 코드

    @Enumerated(EnumType.STRING)    // Enum 값을 문자열로 저장
    private DiscountType discountType;  // 할인 유형

    private Double discountValue;   // 할인 값 (퍼센트 할인율 또는 고정 금액)

    private LocalDateTime startDate;  // 쿠폰 발행일
    private LocalDateTime endDate;    // 쿠폰 만료일
    private Boolean used = false;     // 사용 여부

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    // 할인 적용 로직
    public Double applyDiscount(Double originalPrice) {
        log.info("쿠폰 적용: {} ", discountType);
        DiscountStrategy discountStrategy = getDiscountStrategy();
        return discountStrategy.applyDiscount(originalPrice);
    }

    // 할인 전략 생성 메소드
    private DiscountStrategy getDiscountStrategy() {
        if (discountType == DiscountType.FIXED) {
            return new FixedAmountDiscount(discountValue);
        } else if (discountType == DiscountType.PERCENTAGE) {
            return new PercentageDiscount(discountValue);
        } else {
            throw new IllegalArgumentException("유효하지 않은 할인 유형입니다.");
        }
    }
}