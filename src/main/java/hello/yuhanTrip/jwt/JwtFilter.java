package hello.yuhanTrip.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static int filterInvocationCount = 0;
    public static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer";

    private final TokenProvider tokenProvider;

    // Jwt 토큰의 인증 정보를 현재 쓰레드의 SecurityContext 에 저장하는 역할 수행
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString(); // 요청 ID 생성
        long startTime = System.currentTimeMillis(); // 요청 시작 시간

        log.info("Request ID: {} - 요청 URI: {}에 대한 JWT 필터 시작", requestId, request.getRequestURI());

        filterInvocationCount++;

        // Request Header에서 토큰을 꺼낸다
        String jwt = tokenResolve(request);

        if (jwt == null) {
            log.info("Request ID: {} - 요청 헤더에 JWT 토큰이 없음", requestId);
        } else {
            log.info("Request ID: {} - JWT 토큰 발견: {}", requestId, jwt);
        }

        // validate로 토큰 유효성을 검사
        // 정상 토큰이면 해당 토큰으로 Authentication을 가져와 SecurityContext에 저장
        if (StringUtils.hasText(jwt) && tokenProvider.validate(jwt)) {
            Authentication authentication = tokenProvider.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Request ID: {} - 유효한 JWT 토큰. 인증 정보 설정: {}", requestId, authentication.getName());
        } else {
            if (StringUtils.hasText(jwt)) {
                log.info("Request ID: {} - 유효하지 않은 JWT 토큰", requestId);
            }
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);

        long duration = System.currentTimeMillis() - startTime; // 요청 처리 시간
        log.info("Request ID: {} - 요청 URI: {}에 대한 JWT 필터 종료. 처리 시간: {} ms", requestId, request.getRequestURI(), duration);
    }

    // 요청 헤더에서 토큰 정보 꺼내기
    private String tokenResolve(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER); // 요청 헤더에서 "Authorization" 값을 가져옴
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) { // 헤더 값이 존재하고 "Bearer"로 시작하는지 확인
            return bearerToken.substring(7); // "Bearer" 접두사 제거하고 JWT 문자열 반환
        }
        return null;
    }

    public static int getFilterInvocationCount() {
        return filterInvocationCount;
    }

    public static void resetFilterInvocationCount() {
        filterInvocationCount = 0;
    }
}
