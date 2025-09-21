package com.NH.Fintech.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 개발 단계: CSRF 비활성화 (POST/PUT/DELETE 외부 호출 허용)
                .csrf(csrf -> csrf.disable())
                // 전역 CORS
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                // 세션 미사용
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 권한 규칙
                .authorizeHttpRequests(auth -> auth
                        // Swagger & OpenAPI
                        .requestMatchers(
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
                        ).permitAll()

                        // Health/Actuator(있다면)
                        .requestMatchers(
                                "/actuator/**", "/health", "/health/**"
                        ).permitAll()

                        // 공개 API (MBTI 예시)
                        .requestMatchers(HttpMethod.GET, "/api/mbti/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/mbti/answers").permitAll()

                        // ✅ 소비 로그 API (테스트 위해 전부 허용)
                        .requestMatchers("/api/consumptions/**").permitAll()

                        // ✅ AI Todo 미리보기/커밋 (필요 시 허용)
                        .requestMatchers("/api/ai/todos/**").permitAll()

                        // ✅ 기타 테스트 중인 API들 열어두기
                        .requestMatchers("/api/**").permitAll()

                        // Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 나머지 (개발단계: 전부 허용)
                        .anyRequest().permitAll()
                );

        // httpBasic/formLogin 등을 쓰지 않음 (완전 무상태 API)
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // 프론트 오리진만 지정 (필요 시 추가)
        cfg.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:5173",
                "http://127.0.0.1:5173"
                // "https://fe.your-domain.com",
                // "https://*.your-domain.com"
        ));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization", "Location"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
