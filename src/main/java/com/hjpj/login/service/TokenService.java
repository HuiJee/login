package com.hjpj.login.service;

import com.hjpj.login.dto.UserLogDetail;
import com.hjpj.login.entity.TokenRedis;
import com.hjpj.login.jwt.JwtGenerator;
import com.hjpj.login.jwt.JwtProvider;
import com.hjpj.login.jwt.JwtToken;
import com.hjpj.login.repository.RedisRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtGenerator jwtGenerator;
    private final JwtProvider jwtProvider;
    private final RedisRepository redisRepository;

    public String makeTokenAndCookie(Authentication authentication, UserLogDetail userLogDetail, HttpServletResponse response) {
        try {
            // 위에서 생성한 인증 정보를 토대로 JWT 토큰 생성
            JwtToken token = jwtGenerator.generateToken(authentication);

            // access 토큰은 쿠키 생성해서 클라이언트에 전송
            Cookie accessTokenCookie = new Cookie("accessToken", token.getAccessToken());
            jwtProvider.setTokenCookie(accessTokenCookie);
            response.addCookie(accessTokenCookie);

            // refresh 토큰은 redis 서버에 저장
            redisRepository.save(new TokenRedis(userLogDetail.getUserLogId(), token.getRefreshToken()));

            return token.getAccessToken();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
