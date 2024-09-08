package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon,Long> {

    // 코드가 존재하는지 확인하는 메서드
    boolean existsByCode(String code);
}
