package hello.yuhanTrip.service.discount;
import hello.yuhanTrip.dto.coupon.Coupon;
import hello.yuhanTrip.dto.coupon.DiscountType;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.repository.MemberRepository;
import hello.yuhanTrip.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CouponServiceImplTest {

    @InjectMocks
    private CouponServiceImpl couponService;


    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RedisService redisService;

    private Member member;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        member = new Member();
        member.setId(1L);
    }

    @Test
    public void testGenerateCoupon() {
        // Given
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        Coupon coupon = couponService.generateCoupon(member, DiscountType.PERCENTAGE, 10.0);

        // Then
        assertNotNull(coupon); // 쿠폰이 null이 아님을 확인
        assertEquals(DiscountType.PERCENTAGE, coupon.getDiscountType()); // 쿠폰의 할인 타입이 PERCENTAGE인지 확인
        assertEquals(10.0, coupon.getDiscountValue()); // 쿠폰의 할인 값이 10.0인지 확인
        assertTrue(coupon.getStartDate().isBefore(coupon.getEndDate())); // 시작 날짜가 종료 날짜보다 앞선지 확인

        // Verify coupon is saved in Redis with member and expirationTime
        verify(redisService, times(1)).saveCouponToRedis(eq(member), eq(coupon), any(Long.class));
    }

}