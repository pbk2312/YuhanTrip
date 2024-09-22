package hello.yuhanTrip.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.yuhanTrip.domain.coupon.Coupon;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Log4j2
public class RedisService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper; // ObjectMapper 주입

    public void setStringValue(String memberId, String token, Long expirationTime) {
        ValueOperations<String, String> stringValueOperations = stringRedisTemplate.opsForValue();
        stringValueOperations.set(memberId, token, expirationTime, TimeUnit.MILLISECONDS);
    }

    public void deleteStringValue(String memberId) {
        stringRedisTemplate.delete(memberId);
        log.info("Redis에서 키 삭제 완료: {}", memberId);
    }

    // refreshToken으로 memberId 찾기
    public String findMemberIdByRefreshToken(String refreshToken) {
        ValueOperations<String, String> stringValueOperations = stringRedisTemplate.opsForValue();
        // 모든 키를 순회하면서 refreshToken과 일치하는 값을 찾는다
        for (String key : stringRedisTemplate.keys("*")) {
            String storedToken = stringValueOperations.get(key);
            if (refreshToken.equals(storedToken)) {
                log.info("Redis에서 refreshToken으로 memberId 찾기 완료: {}", key);
                return key; // memberId 반환
            }
        }
        return null;
    }

    // 쿠폰을 Redis에 저장하는 메서드
    public void saveCouponToRedis(Long couponId, Coupon coupon, Long expirationTime) {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String couponJson = convertCouponToJson(coupon);
        if (couponJson != null) {
            valueOperations.set("coupon:" + couponId, couponJson, expirationTime, TimeUnit.MILLISECONDS);
            log.info("쿠폰이 Redis에 저장되었습니다. couponId: {}", couponId);
        } else {
            log.error("쿠폰을 JSON으로 변환하는 데 실패했습니다.");
        }
    }

    // Redis에서 쿠폰을 조회하는 메서드
    public Coupon getCouponFromRedis(Long couponId) {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String couponJson = valueOperations.get("coupon:" + couponId);
        if (couponJson != null) {
            return convertJsonToCoupon(couponJson);
        } else {
            log.info("Redis에서 쿠폰을 찾을 수 없습니다. couponId: {}", couponId);
            return null;
        }
    }

    // Redis에서 쿠폰을 삭제하는 메서드
    public void deleteCouponFromRedis(Long couponId) {
        String key = "coupon:" + couponId;
        Boolean isDeleted = stringRedisTemplate.delete(key);
        if (isDeleted != null && isDeleted) {
            log.info("Redis에서 쿠폰이 삭제되었습니다. couponId: {}", couponId);
        } else {
            log.warn("Redis에서 쿠폰을 삭제하는 데 실패했습니다. couponId: {}", couponId);
        }
    }

    // 쿠폰 정보를 JSON으로 변환하는 메서드
    private String convertCouponToJson(Coupon coupon) {
        try {
            return objectMapper.writeValueAsString(coupon);
        } catch (JsonProcessingException e) {
            log.error("쿠폰을 JSON으로 변환하는 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    // JSON 문자열을 쿠폰 객체로 변환하는 메서드
    private Coupon convertJsonToCoupon(String couponJson) {
        try {
            return objectMapper.readValue(couponJson, Coupon.class);
        } catch (JsonProcessingException e) {
            log.error("JSON을 쿠폰으로 변환하는 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }
}
