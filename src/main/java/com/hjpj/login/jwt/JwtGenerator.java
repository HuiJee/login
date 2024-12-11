package com.hjpj.login.jwt;

import com.hjpj.login.dto.UserLogDetail;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtGenerator {

    private final Key key;

    long now = (new Date()).getTime();

    /** application.yaml 에서 secret 값 가져와 key에 저장 */
    public JwtGenerator(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /** 로그인 시 받아온 header 정보를 통해 AccessToken, RefreshToken 생성 */
    public JwtToken generateToken(Authentication authentication) {
        // 권한 가져오기
        String authoritites = getAuthorities(authentication);

        // authentication에 담아놓은 계정 정보 추출
        UserLogDetail userLogDetail = (UserLogDetail) authentication.getPrincipal();

        // 최초 accessToken 생성
        String accessToken = generateToken(authoritites, userLogDetail, JwtUtil.ACCESS_TOKEN_VALIDITY);

        // RefreshToken 생성
        String refreshToken = generateToken(authoritites, userLogDetail, JwtUtil.REFRESH_TOKEN_VALIDITY);

        return JwtToken.builder()
                .grantType(JwtUtil.GRANT_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /** 토큰 생성하기 */
    public String generateToken(String authorities, UserLogDetail userLogDetail, long validTime) {
        return Jwts.builder()
                .setSubject(userLogDetail.getUserLogId())
                .claim("Auth", authorities)
                .claim("UserLogId", userLogDetail.getUserLogId())
                .setExpiration(new Date(now + validTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    
    /** 권한 추출 메서드 */
    public String getAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }
}
