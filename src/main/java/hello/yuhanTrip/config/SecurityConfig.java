package hello.yuhanTrip.config;

import hello.yuhanTrip.jwt.JwtSecurityConfig;
import hello.yuhanTrip.jwt.TokenProvider;
import hello.yuhanTrip.jwt.handler.JwtAccessDeniedHandler;
import hello.yuhanTrip.jwt.handler.JwtAuthenticationEntryPoint;
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

    private final TokenProvider tokenProvider;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/member/withdrawalMembership").authenticated()
                        .requestMatchers("/member/deleteAccount").authenticated()
                        .requestMatchers("/api/likes").authenticated()
                        .requestMatchers("/mypage/accommodationByMember").hasRole("HOST")
                        .requestMatchers("/paymentPage").authenticated()
                        .requestMatchers("paymentCancelPage").authenticated()
                        .requestMatchers("/mypage").authenticated()
                        .requestMatchers("reservation").authenticated()
                        .requestMatchers("/reservation/submit").authenticated()
                        .requestMatchers("/reservationConfirm").authenticated()
                        .requestMatchers("/reservationUpdate").authenticated()
                        .requestMatchers("/updateReservation").authenticated()
                        .requestMatchers("/reservationConfirm/cancel").authenticated()
                        .requestMatchers("/reviewWrite").authenticated()
                        .requestMatchers("/submitReview").authenticated()
                        .requestMatchers("/myReviews").authenticated()
                        .requestMatchers("/accommodation/registerForm").hasRole("HOST")
                        .requestMatchers("/mypage/memberAccommodations").hasRole("HOST")
                        .anyRequest().permitAll()
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .with(new JwtSecurityConfig(tokenProvider), jwtSecurityConfig -> {
                    // JwtSecurityConfig에 대한 커스터마이즈 작업을 수행합니다.
                });

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}