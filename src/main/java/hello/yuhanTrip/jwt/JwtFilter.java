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

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer";

    private final TokenProvider tokenProvider;

    // Jwt 토큰의 인증 정보를 현재 쓰레드의 SecurityContext 에 저장하는 역할 수행
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("요청 URI: {}에 대한 JWT 필터 시작", request.getRequestURI());

        // Request Header에서 토큰을 꺼낸다
        String jwt = tokenResolve(request);

        if (jwt == null) {
            log.info("요청 헤더에 JWT 토큰이 없음");
        } else {
            log.info("JWT 토큰 발견: {}", jwt);
        }

        // validate로 토큰 유효성을 검사
        // 정상 토큰이면 해당 토큰으로 Authentication을 가져와 SecurityContext에 저장
        if (StringUtils.hasText(jwt) && tokenProvider.validate(jwt)) {
            Authentication authentication = tokenProvider.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("유효한 JWT 토큰. 인증 정보 설정: {}", authentication.getName());
        } else {
            if (StringUtils.hasText(jwt)) {
                log.info("유효하지 않은 JWT 토큰");
            }
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);

        log.info("요청 URI: {}에 대한 JWT 필터 종료", request.getRequestURI());
    }

    // 요청 헤더에서 토큰 정보 꺼내기
    private String tokenResolve(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER); // 요청 헤더에서 "Authorization" 값을 가져옴
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) { // 헤더 값이 존재하고 "Bearer"로 시작하는지 확인
            return bearerToken.substring(7); // "Bearer" 접두사 제거하고 JWT 문자열 반환
        }
        return null;
    }
}
