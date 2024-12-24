package com.hjpj.login.service;

import com.hjpj.login.dto.UserLogDetail;
import com.hjpj.login.entity.TokenRedis;
import com.hjpj.login.exception.CustomException;
import com.hjpj.login.exception.ErrorCode;
import com.hjpj.login.jwt.JwtGenerator;
import com.hjpj.login.jwt.JwtProvider;
import com.hjpj.login.jwt.JwtToken;
import com.hjpj.login.jwt.JwtUtil;
import com.hjpj.login.repository.RedisRepository;
import com.hjpj.login.util.CommonUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtGenerator jwtGenerator;
    private final JwtProvider jwtProvider;
    private final RedisRepository redisRepository;

    public void makeTokenAndCookie(Authentication authentication, UserLogDetail userLogDetail, HttpServletResponse response) {
        // 위에서 생성한 인증 정보를 토대로 JWT 토큰 생성
        JwtToken token = jwtGenerator.generateToken(authentication, userLogDetail.getAutoLogin());

        // access 토큰은 쿠키 생성해서 클라이언트에 전송
        setTokenCookie(response, token.getAccessToken());

        // refresh 토큰은 redis 서버에 저장
        redisRepository.save(new TokenRedis(userLogDetail.getUserLogId(), token.getRefreshToken()));
    }

    /** 헤더에서 logId 얻어 토큰 작업하기 */
    public void getAccessFromRefreshByUserLogId(HttpServletRequest request, HttpServletResponse response) {
        String userLogId = request.getHeader(CommonUtil.USER_LOG_ID_NAME);

        getAccessFromRefresh(userLogId, response);
    }

    /** refresh 토큰으로부터 access 토큰 생성하여 쿠키에 담기 (공통 사용)*/
    public void getAccessFromRefresh(String userLogId, HttpServletResponse response) {
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
            setTokenCookie(response, accessToken);

        } else {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    public void setTokenCookie(HttpServletResponse response, String token) {
        Cookie accessTokenCookie = new Cookie(CommonUtil.ACCESS_TOKEN, token);
        jwtGenerator.setTokenCookie(accessTokenCookie);
        response.addCookie(accessTokenCookie);
    }
}
