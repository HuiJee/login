package com.hjpj.login.config;

import com.hjpj.login.jwt.JwtAuthenticationFilter;
import com.hjpj.login.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity  //Spring Security 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    /** 비밀번호 암호화를 위한 Bean 생성 */
    @Bean
    public PasswordEncoder passwordEncoder() {return new BCryptPasswordEncoder();}

    /** HTTP 요청에 대해 특정 보안 필터를 설정하는 역할 */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        System.out.println("SecurityConfit의 filterChain 실행");
        return httpSecurity
                // HTTP Basic 인증 비활성화 (JWT 사용이므로)
                .httpBasic(AbstractHttpConfigurer::disable)

                // CSRF 보호 비활성화 (JWT 기반 인증에서는 불필요)
                .csrf(AbstractHttpConfigurer::disable)

                // 기본 form 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // 세션 관리를 Stateless로 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/static/**", "/public/**", "/resources/static/**", "/META-INF/resources/**",
                                "/css/**", "/js/**", "/bootstrap/**", "/images/**", "/icons/**", "/fonts/**", "/favicon.ico").permitAll()
                        .requestMatchers("/login/**", "/").permitAll()
                        .anyRequest().authenticated()
                )

                // 사용자 정의 JWT 필터 추가 (인증 후 실행)
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
