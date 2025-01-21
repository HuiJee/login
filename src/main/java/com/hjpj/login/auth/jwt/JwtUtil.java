package com.hjpj.login.auth.jwt;

import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    public static final String GRANT_TYPE = "Bearer";
    public static final String INITIAL_TYPE = "Basic";

    public static final long ACCESS_TOKEN_VALIDITY = 1000 * 60;   // 1분 (테스트용)
//    public static final long ACCESS_TOKEN_VALIDITY = 1000 * 60 * 60;   // 1시간
    
    // 자동로그인 설정한 경우 (크기가 INT를 초과하므로 L을 붙여줘야 함)
    public static final long REFRESH_TOKEN_VALIDITY = 1000L * 60 * 60 * 24 * 30;  // 30일
    // 자동로그인 미설정인 경우
    public static final long SHORT_REFRESH_VALIDITY = 1000 * 60 * 60 * 24; // 1일

}
