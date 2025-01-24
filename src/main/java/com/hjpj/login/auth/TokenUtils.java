package com.hjpj.login.auth;

import com.hjpj.login.auth.jwt.JwtGenerator;
import com.hjpj.login.auth.jwt.JwtProvider;
import com.hjpj.login.auth.jwt.JwtToken;
import com.hjpj.login.auth.jwt.JwtUtil;
import com.hjpj.login.common.CommonUtils;
import com.hjpj.login.exception.CustomException;
import com.hjpj.login.exception.ErrorCode;
import com.hjpj.login.redis.RedisRepository;
import com.hjpj.login.redis.TokenRedis;
import com.hjpj.login.user.dto.UserLogDetail;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TokenUtils {

    private static JwtGenerator jwtGenerator;
    private static RedisRepository redisRepository;
    private static JwtProvider jwtProvider;

    @Autowired
    public TokenUtils(JwtGenerator jwtGenerator, RedisRepository redisRepository, JwtProvider jwtProvider) {
        TokenUtils.jwtGenerator = jwtGenerator;
        TokenUtils.redisRepository = redisRepository;
        TokenUtils.jwtProvider = jwtProvider;
    }

    /** 토큰 만들어서 쿠키 및 redis에 저장*/
    public static void makeTokenAndCookie(Authentication authentication, UserLogDetail userLogDetail, HttpServletResponse response) {
        // 위에서 생성한 인증 정보를 토대로 JWT 토큰 생성
        JwtToken token = jwtGenerator.generateToken(authentication, userLogDetail.getAutoLogin());

        // access 토큰은 쿠키 생성해서 클라이언트에 전송
        setTokenCookie(response, token.getAccessToken());

        // refresh 토큰은 redis 서버에 저장
        redisRepository.save(new TokenRedis(userLogDetail.getUserLogId(), token.getRefreshToken()));
    }

    /** 토큰 쿠키 셋팅 */
    public static void setTokenCookie(HttpServletResponse response, String token) {
        Cookie accessTokenCookie = new Cookie(CommonUtils.ACCESS_TOKEN, token);
        jwtGenerator.setTokenCookie(accessTokenCookie);
        response.addCookie(accessTokenCookie);
    }

    /** refresh 토큰으로부터 access 토큰 생성하여 쿠키에 담기 (공통 사용)*/
    public static void getAccessFromRefresh(String userLogId, HttpServletResponse response) {
        TokenRedis tokenRedis = redisRepository.findById(userLogId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSING_TOKEN));

        String refresh = tokenRedis.getRefreshToken();

        if(StringUtils.hasText(refresh) && jwtProvider.validateToken(refresh)) {

            Authentication authentication = jwtProvider.getAuthentication(refresh);

            // refresh 속 권한 가져오기
            String authoritites = jwtGenerator.getAuthorities(authentication);
            // refresh 속 사용자 정보 가져오기
            UserLogDetail userLogDetail = (UserLogDetail) authentication.getPrincipal();
            // accessToken만 발급
            String accessToken = jwtGenerator.generateToken(authoritites, userLogDetail, JwtUtil.ACCESS_TOKEN_VALIDITY);
            // access 토큰은 쿠키 생성해서 클라이언트에 전송
            TokenUtils.setTokenCookie(response, accessToken);

        } else {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }
}
