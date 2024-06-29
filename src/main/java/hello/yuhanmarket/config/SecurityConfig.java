package hello.yuhanmarket.config;

import hello.yuhanmarket.jwt.JwtSecurityConfig;
import hello.yuhanmarket.jwt.TokenProvider;
import hello.yuhanmarket.jwt.handler.JwtAccessDeniedHandler;
import hello.yuhanmarket.jwt.handler.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    // 필요한 의존성 주입
    private final TokenProvider tokenProvider;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    // 패스워드 인코더 빈 설정
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 보안 필터 체인 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 인증 에러 처리
                        .accessDeniedHandler(jwtAccessDeniedHandler) // 권한 에러 처리
                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin()) // X-Frame-Options 설정
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 관리 설정 (무상태 세션)
                )
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/mypage/**").authenticated() // 특정 URL 패턴의 요청은 인증이 필요
                        .requestMatchers("/member/**").permitAll() // 특정 URL 패턴의 요청은 모든 사용자에게 허용
                        .requestMatchers("/email/**").permitAll() // 특정 URL 패턴의 요청은 모든 사용자에게 허용
                        .requestMatchers("/market/**").permitAll() // 특정 URL 패턴의 요청은 모든 사용자에게 허용 (수정된 부분)
                        .requestMatchers("/admin/**").authenticated() // 특정 URL 패턴의 요청은 모든 사용자에게 허용 (수정된 부분)
                        .requestMatchers("/board/**").permitAll()
                        .requestMatchers("/thymeleaf/**").permitAll()
                        .anyRequest().authenticated() // 그 외의 모든 요청은 인증이 필요
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 추가

                // JwtFilter 를 addFilterBefore 로 등록했던 JwtSecurityConfig 클래스를 적용
                .with(new JwtSecurityConfig(tokenProvider), jwtSecurityConfig -> {
                    // JwtSecurityConfig에 대한 커스터마이즈 작업을 수행합니다.
                });

        return http.build();
    }

    // 정적 리소스에 대한 보안 설정
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        log.info("---------web configure---------");
        return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    // CORS 설정을 위한 ConfigurationSource 빈 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*"); // 모든 도메인 허용
        configuration.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
        configuration.addAllowedHeader("*"); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // 인증 정보 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}