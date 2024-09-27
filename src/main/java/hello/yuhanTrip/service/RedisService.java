package hello.yuhanTrip.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.yuhanTrip.dto.coupon.Coupon;
import hello.yuhanTrip.domain.member.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    public void saveCouponToRedis(Member member,Coupon coupon, Long expirationTime) {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String couponJson = convertCouponToJson(coupon);
        if (couponJson != null) {
            valueOperations.set("coupon:" + member.getId()+ ":" +  coupon.getCode(), couponJson, expirationTime, TimeUnit.MILLISECONDS);
            log.info("쿠폰이 Redis에 저장되었습니다. couponCode: {}", coupon.getCode());
        } else {
            log.error("쿠폰을 JSON으로 변환하는 데 실패했습니다.");
        }
    }

    // Redis에서 멤버의 모든 쿠폰을 리스트로 반환하는 메서드
    public List<Coupon> getCouponsFromRedis(Member member) {
        List<Coupon> coupons = new ArrayList<>();
        String keyPattern = "coupon:" + member.getId() + ":*"; // 멤버 ID에 해당하는 쿠폰 키 패턴

        // 키 패턴에 맞는 모든 쿠폰 키를 조회
        Set<String> keys = stringRedisTemplate.keys(keyPattern);

        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
                String couponJson = valueOperations.get(key); // 쿠폰 JSON 문자열 가져오기

                if (couponJson != null) {
                    // JSON 문자열을 Coupon 객체로 변환
                    Coupon coupon = convertJsonToCoupon(couponJson);
                    if (coupon != null) { // 변환 성공 시 리스트에 추가
                        coupons.add(coupon);
                    }
                }
            }
        } else {
            log.info("Redis에서 멤버의 쿠폰을 찾을 수 없습니다. memberId: {}", member.getId());
        }

        return coupons;
    }

    // Redis에서 쿠폰을 조회하는 메서드
    // Redis에서 쿠폰을 조회하는 메서드
    public Coupon getFromRedis(String couponCode, Member member) {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String key = "coupon:" + member.getId() + ":" + couponCode; // memberId와 couponCode로 키 생성
        String couponJson = valueOperations.get(key); // Redis에서 쿠폰 JSON 가져오기

        if (couponJson != null) {
            Coupon coupon = convertJsonToCoupon(couponJson); // JSON 문자열을 Coupon 객체로 변환
            log.info("Redis에서 쿠폰을 찾았습니다. memberId: {}, couponCode: {}", member.getId(), couponCode); // 성공 로그 추가
            return coupon;
        } else {
            log.info("Redis에서 쿠폰을 찾을 수 없습니다. memberId: {}, couponCode: {}", member.getId(), couponCode);
            return null;
        }
    }

    // 특정 회원이 이미 쿠폰을 발급받았는지 확인하는 메서드
    public boolean hasCoupon(Long memberId) {
        String keyPattern = "coupon:" + memberId + ":*"; // 회원 ID에 해당하는 쿠폰 키 패턴
        Set<String> keys = stringRedisTemplate.keys(keyPattern); // 패턴에 맞는 쿠폰 키를 조회

        // 쿠폰 키가 존재하면 true, 없으면 false 반환
        return keys != null && !keys.isEmpty();
    }
    // Redis에서 쿠폰을 삭제하는 메서드
    public void deleteCouponFromRedis(String couponCode, Member member) {
        String key = "coupon:" + member.getId() + ":" + couponCode; // memberId와 couponCode로 키 생성
        Boolean isDeleted = stringRedisTemplate.delete(key); // Redis에서 쿠폰 삭제

        if (isDeleted != null && isDeleted) {
            log.info("Redis에서 쿠폰이 삭제되었습니다. memberId: {}, couponCode: {}", member.getId(), couponCode);
        } else {
            log.warn("Redis에서 쿠폰을 삭제하는 데 실패했습니다. memberId: {}, couponCode: {}", member.getId(), couponCode);
        }
    }
    // 이메일 인증번호를 Redis에 저장하는 메서드
    public void saveEmailCertificationToRedis(String email, String certificationNumber) {
        String value = certificationNumber + ":false"; // 초기 상태는 false
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.set("emailCertification:" + email, value, 60 * 60, TimeUnit.SECONDS); // 1시간 유효
        log.info("이메일 인증번호가 Redis에 저장되었습니다. email: {}", email);
    }
    // 이메일 인증번호를 Redis에서 가져오는 메서드
    public String getEmailCertificationFromRedis(String email) {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String key = "emailCertification:" + email; // 이메일을 기반으로 키 생성
        String certificationNumber = valueOperations.get(key); // Redis에서 인증번호 가져오기

        if (certificationNumber != null) {
            log.info("Redis에서 인증번호를 찾았습니다. email: {}, 인증번호: {}", email, certificationNumber);
            return certificationNumber;
        } else {
            log.warn("Redis에서 인증번호를 찾을 수 없습니다. email: {}", email);
            return null;
        }
    }

    public void updateEmailCertificationInRedis(String email, String updatedValue) {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.set("emailCertification:" + email, updatedValue);
        log.info("이메일 인증 상태가 Redis에서 업데이트되었습니다. email: {}", email);
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
