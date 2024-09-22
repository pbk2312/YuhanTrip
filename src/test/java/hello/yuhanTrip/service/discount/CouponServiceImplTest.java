package hello.yuhanTrip.service.discount;
import hello.yuhanTrip.domain.coupon.Coupon;
import hello.yuhanTrip.domain.coupon.DiscountType;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.repository.CouponRepository;
import hello.yuhanTrip.repository.MemberRepository;
import hello.yuhanTrip.service.RedisService;
import hello.yuhanTrip.service.discount.CouponServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CouponServiceImplTest {

    @InjectMocks
    private CouponServiceImpl couponService;

    @Mock
    private CouponRepository couponRepository;

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
        member.setCoupons(new ArrayList<>());
    }

    @Test
    public void testGenerateCoupon() {
        // Given
        when(couponRepository.existsByCode(any())).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        Coupon coupon = couponService.generateCoupon(member, DiscountType.PERCENTAGE, 10.0);

        // Then
        assertNotNull(coupon);
        assertEquals(DiscountType.PERCENTAGE, coupon.getDiscountType());
        assertEquals(10.0, coupon.getDiscountValue());
        assertTrue(coupon.getStartDate().isBefore(coupon.getEndDate()));

        // Verify coupon is saved in Redis
        verify(redisService, times(1)).saveCouponToRedis(eq(coupon.getId()), any(Coupon.class), any(Long.class));
    }
}