package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.coupon.Coupon;
import hello.yuhanTrip.domain.coupon.DiscountType;
import hello.yuhanTrip.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon,Long> {

    // 코드가 존재하는지 확인하는 메서드
    boolean existsByCode(String code);

    // 회원과 할인 유형에 따라 미사용 쿠폰이 존재하는지 확인하는 메서드
    boolean existsByMemberAndDiscountTypeAndUsed(Member member, DiscountType discountType, Boolean used);
}
