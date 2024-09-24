package hello.yuhanTrip.dto.member;


import hello.yuhanTrip.dto.coupon.DiscountType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CouponDTO {

    private Long id;
    private String code;            // 쿠폰 코드
    private DiscountType discountType;  // 할인 유형
    private Double discountValue;   // 할인 값 (퍼센트 할인율 또는 고정 금액)
    private LocalDateTime startDate;  // 쿠폰 발행일
    private LocalDateTime endDate;    // 쿠폰 만료일
    private Boolean used;     // 사용 여부

    private Long memberId;   // 연관된 Member의 ID

}
